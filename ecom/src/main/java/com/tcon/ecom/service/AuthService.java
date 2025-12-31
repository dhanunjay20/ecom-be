package com.tcon.ecom.service;

import com.tcon.ecom.dto.request.*;
import com.tcon.ecom.dto.response.AuthResponse;
import com.tcon.ecom.dto.response.TokenResponse;
import com.tcon.ecom.dto.response.UserResponse;
import com.tcon.ecom.exception.BadRequestException;
import com.tcon.ecom.exception.EmailAlreadyExistsException;
import com.tcon.ecom.exception.ResourceNotFoundException;
import com.tcon.ecom.exception.UnauthorizedException;
import com.tcon.ecom.model.User;
import com.tcon.ecom.model.enums.UserRole;
import com.tcon.ecom.model.enums.UserStatus;
import com.tcon.ecom.model.enums.VendorStatus;
import com.tcon.ecom.repository.UserRepository;
import com.tcon.ecom.repository.VendorRepository;
import com.tcon.ecom.security.JwtTokenProvider;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.modelmapper.ModelMapper;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class AuthService {

    private final UserRepository userRepository;
    private final VendorRepository vendorRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtTokenProvider tokenProvider;
    private final AuthenticationManager authenticationManager;
    private final EmailService emailService;
    private final ModelMapper modelMapper;

    @Transactional
    public AuthResponse register(RegisterRequest request) {
        // Validate passwords match
        if (!request.getPassword().equals(request.getConfirmPassword())) {
            throw new BadRequestException("Passwords do not match");
        }

        // Check if email already exists
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new EmailAlreadyExistsException(request.getEmail());
        }

        // Create new user
        User user = new User();
        user.setEmail(request.getEmail());
        user.setPassword(passwordEncoder.encode(request.getPassword()));
        user.setFirstName(request.getFirstName());
        user.setLastName(request.getLastName());
        user.setPhone(request.getPhone());
        // Set role - default to CUSTOMER if not specified or invalid
        if (request.getRole() != null && request.getRole().equalsIgnoreCase("vendor")) {
            user.setRole(UserRole.VENDOR);
        } else {
            user.setRole(UserRole.CUSTOMER);
        }
        user.setStatus(UserStatus.ACTIVE);
        user.setEmailVerified(false);
        user.setLoginAttempts(0);

        // Generate email verification token
        String verificationToken = UUID.randomUUID().toString();
        user.setEmailVerificationToken(verificationToken);
        user.setEmailVerificationExpiry(LocalDateTime.now().plusHours(24));

        // Save user
        user = userRepository.save(user);

        // Send verification email (non-blocking - don't fail if email fails)
        try {
            emailService.sendVerificationEmail(user.getEmail(), verificationToken, user.getFirstName());
            log.info("Verification email sent to: {}", user.getEmail());
        } catch (Exception e) {
            log.error("Failed to send verification email to: {}. Registration will continue.", user.getEmail(), e);
            // Don't throw - allow registration to succeed even if email fails
        }

        // Generate tokens
        String accessToken = tokenProvider.generateAccessToken(user.getEmail());
        String refreshToken = tokenProvider.generateRefreshToken(user.getEmail());

        // Save refresh token
        user.setRefreshToken(refreshToken);
        userRepository.save(user);

        // Build response
        UserResponse userResponse = modelMapper.map(user, UserResponse.class);
        userResponse.setMemberSince(user.getCreatedAt());

        return AuthResponse.builder()
                .user(userResponse)
                .accessToken(accessToken)
                .refreshToken(refreshToken)
                .isNewUser(true)
                .build();
    }

    @Transactional
    public AuthResponse login(LoginRequest request) {
        log.info("🔐 Login attempt for email: {}", request.getEmail());

        User user = userRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> {
                    log.warn("❌ Login failed: User not found with email: {}", request.getEmail());
                    return new BadCredentialsException("Invalid email or password");
                });

        log.info("✅ User found: {} (Status: {}, EmailVerified: {}, Role: {})",
                 user.getEmail(), user.getStatus(), user.getEmailVerified(), user.getRole());

        // Check if account is locked
        if (!user.isAccountNonLocked()) {
            log.warn("🔒 Login failed: Account is locked for: {}", user.getEmail());
            throw new UnauthorizedException("Account is temporarily locked. Please try again later.");
        }

        // Check if email is verified - THIS IS THE CRITICAL CHECK!
        if (Boolean.FALSE.equals(user.getEmailVerified())) {
            log.warn("📧 Login failed: Email not verified for: {}", user.getEmail());
            throw new UnauthorizedException("Please verify your email address before logging in. Check your inbox for the verification link.");
        }

        // FIX: If email is verified but status is still PENDING, activate the account
        // This handles edge case where verification updated emailVerified but not status
        if (Boolean.TRUE.equals(user.getEmailVerified()) && user.getStatus() == UserStatus.PENDING) {
            log.warn("⚠️ FIXING DATA INCONSISTENCY: Email verified but status still PENDING - Auto-activating account for: {}", user.getEmail());
            user.setStatus(UserStatus.ACTIVE);
            userRepository.save(user);

            // Also approve vendor if applicable
            if (user.getRole() == UserRole.VENDOR && user.getVendorProfile() != null) {
                vendorRepository.findById(user.getVendorProfile()).ifPresent(vendor -> {
                    if (vendor.getStatus() == VendorStatus.PENDING) {
                        log.info("🔧 Auto-approving vendor profile for: {}", user.getEmail());
                        vendor.setStatus(VendorStatus.APPROVED);
                        vendor.setApprovedAt(LocalDateTime.now());
                        vendor.setApprovedBy("SYSTEM-AUTO");
                        vendorRepository.save(vendor);
                        log.info("✅ Vendor auto-approved!");
                    }
                });
            }
            log.info("✅ Account auto-activated successfully! User can now login.");
        }

        // Check if account is active (vendors must be active after email verification)
        if (user.getStatus() != UserStatus.ACTIVE) {
            log.warn("⚠️ Login failed: Account status is {} for: {}", user.getStatus(), user.getEmail());
            throw new UnauthorizedException("Account is " + user.getStatus().name().toLowerCase() + ". Please contact support if you believe this is an error.");
        }

        try {
            log.info("🔑 Authenticating user: {}", user.getEmail());

            // Authenticate
            Authentication authentication = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(request.getEmail(), request.getPassword())
            );

            log.info("✅ Authentication successful for: {}", user.getEmail());

            // Reset login attempts on successful login
            user.setLoginAttempts(0);
            user.setLockUntil(null);
            user.setLastLogin(LocalDateTime.now());

            // Generate tokens
            String accessToken = tokenProvider.generateAccessToken(authentication);
            String refreshToken = tokenProvider.generateRefreshToken(user.getEmail());

            // Save refresh token
            user.setRefreshToken(refreshToken);
            userRepository.save(user);

            log.info("🎉 Login successful for: {} (Role: {})", user.getEmail(), user.getRole());

            // Build response
            UserResponse userResponse = modelMapper.map(user, UserResponse.class);
            userResponse.setMemberSince(user.getCreatedAt());

            return AuthResponse.builder()
                    .user(userResponse)
                    .accessToken(accessToken)
                    .refreshToken(refreshToken)
                    .isNewUser(false)
                    .build();

        } catch (org.springframework.security.authentication.DisabledException e) {
            // This happens when isEnabled() returns false in UserPrincipal
            log.error("❌ Account disabled exception for: {}", user.getEmail());

            if (Boolean.FALSE.equals(user.getEmailVerified())) {
                throw new UnauthorizedException("Please verify your email address before logging in. Check your inbox for the verification link.");
            } else if (user.getStatus() == UserStatus.PENDING) {
                throw new UnauthorizedException("Your account is pending approval. Please wait for admin approval or verify your email.");
            } else if (user.getStatus() == UserStatus.SUSPENDED) {
                throw new UnauthorizedException("Your account has been suspended. Please contact support.");
            } else {
                throw new UnauthorizedException("Your account is currently disabled. Please contact support.");
            }
        } catch (BadCredentialsException e) {
            log.warn("❌ Invalid password for: {}", user.getEmail());

            // Increment login attempts
            user.setLoginAttempts(user.getLoginAttempts() + 1);

            // Lock account after 3 failed attempts for 15 minutes
            if (user.getLoginAttempts() >= 3) {
                user.setLockUntil(LocalDateTime.now().plusMinutes(15));
                userRepository.save(user);
                log.warn("🔒 Account locked after 3 failed attempts: {}", user.getEmail());
                throw new UnauthorizedException("Too many failed login attempts. Account locked for 15 minutes.");
            }

            userRepository.save(user);
            throw new BadCredentialsException("Invalid email or password");
        }
    }

    @Transactional
    public void verifyEmail(String token) {
        log.info("╔════════════════════════════════════════════════════════════════════════════╗");
        log.info("║              EMAIL VERIFICATION PROCESS STARTED                            ║");
        log.info("╚════════════════════════════════════════════════════════════════════════════╝");
        log.info("Token: {}", token);

        User user = userRepository.findByEmailVerificationToken(token)
                .orElseThrow(() -> new BadRequestException("Invalid or expired verification token"));

        log.info("┌─────────────────────────────────────────────────────────────────────────────┐");
        log.info("│ BEFORE VERIFICATION:                                                        │");
        log.info("│ Email: {}                                              ", user.getEmail());
        log.info("│ User ID: {}                                  ", user.getId());
        log.info("│ Role: {}                                                              ", user.getRole());
        log.info("│ Email Verified: {}                                                      ", user.getEmailVerified());
        log.info("│ User Status: {}                                                       ", user.getStatus());
        log.info("│ Vendor Profile ID: {}                               ", user.getVendorProfile());
        log.info("└─────────────────────────────────────────────────────────────────────────────┘");

        // Check if token expired
        if (user.getEmailVerificationExpiry().isBefore(LocalDateTime.now())) {
            log.error("❌ Verification token has expired!");
            throw new BadRequestException("Verification token has expired");
        }

        // Store original status for comparison
        UserStatus originalUserStatus = user.getStatus();
        boolean originalEmailVerified = user.getEmailVerified();

        // Verify email
        user.setEmailVerified(true);
        user.setEmailVerificationToken(null);
        user.setEmailVerificationExpiry(null);

        // Activate user account after email verification
        user.setStatus(UserStatus.ACTIVE);

        log.info("⏳ Saving user with updated status...");

        User savedUser = userRepository.save(user);

        log.info("┌─────────────────────────────────────────────────────────────────────────────┐");
        log.info("│ USER STATUS UPDATED:                                                        │");
        log.info("│ Email Verified: {} → {}                                          ", originalEmailVerified, savedUser.getEmailVerified());
        log.info("│ User Status: {} → {}                                      ", originalUserStatus, savedUser.getStatus());
        log.info("└─────────────────────────────────────────────────────────────────────────────┘");

        // If user is a vendor, approve the vendor profile automatically
        if (user.getRole() == UserRole.VENDOR && user.getVendorProfile() != null) {
            log.info("👤 User has VENDOR role - Processing vendor profile approval...");
            log.info("📋 Vendor Profile ID: {}", user.getVendorProfile());

            vendorRepository.findById(user.getVendorProfile()).ifPresentOrElse(
                vendor -> {
                    VendorStatus originalVendorStatus = vendor.getStatus();

                    log.info("┌─────────────────────────────────────────────────────────────────────────────┐");
                    log.info("│ VENDOR PROFILE BEFORE APPROVAL:                                             │");
                    log.info("│ Vendor ID: {}                                ", vendor.getId());
                    log.info("│ Store Name: {}                                              ", vendor.getStoreName());
                    log.info("│ Email: {}                                      ", vendor.getEmail());
                    log.info("│ Status: {}                                                      ", vendor.getStatus());
                    log.info("└─────────────────────────────────────────────────────────────────────────────┘");

                    vendor.setStatus(VendorStatus.APPROVED);
                    vendor.setApprovedAt(LocalDateTime.now());
                    vendor.setApprovedBy("SYSTEM"); // Auto-approved on email verification

                    log.info("⏳ Saving vendor profile with APPROVED status...");

                    com.tcon.ecom.model.Vendor savedVendor = vendorRepository.save(vendor);

                    log.info("┌─────────────────────────────────────────────────────────────────────────────┐");
                    log.info("│ VENDOR PROFILE UPDATED:                                                     │");
                    log.info("│ Status: {} → {}                                      ", originalVendorStatus, savedVendor.getStatus());
                    log.info("│ Approved At: {}                                ", savedVendor.getApprovedAt());
                    log.info("│ Approved By: {}                                                      ", savedVendor.getApprovedBy());
                    log.info("└─────────────────────────────────────────────────────────────────────────────┘");

                    log.info("✅ Vendor profile approved successfully!");
                },
                () -> {
                    log.warn("⚠️  Vendor profile not found for user: {} (Vendor ID: {})",
                               user.getEmail(), user.getVendorProfile());
                }
            );
        } else {
            log.info("ℹ️  User is not a vendor or has no vendor profile");
            log.info("   Role: {}, VendorProfile: {}", user.getRole(), user.getVendorProfile());
        }

        log.info("╔════════════════════════════════════════════════════════════════════════════╗");
        log.info("║              VERIFICATION SUMMARY                                          ║");
        log.info("╠════════════════════════════════════════════════════════════════════════════╣");
        log.info("║ ✅ Email: {}                                     ", savedUser.getEmail());
        log.info("║ ✅ Email Verified: {}                                                   ", savedUser.getEmailVerified());
        log.info("║ ✅ User Status: {}                                                    ", savedUser.getStatus());
        if (user.getRole() == UserRole.VENDOR) {
            vendorRepository.findById(user.getVendorProfile()).ifPresent(v -> {
                log.info("║ ✅ Vendor Status: {}                                               ", v.getStatus());
                log.info("║ ✅ Can Login: YES                                                          ║");
            });
        } else {
            log.info("║ ✅ Can Login: YES                                                          ║");
        }
        log.info("╚════════════════════════════════════════════════════════════════════════════╝");

        // Send welcome email
        try {
            log.info("📧 Sending welcome email to: {}", user.getEmail());
            emailService.sendWelcomeEmail(user.getEmail(), user.getFirstName());
            log.info("✅ Welcome email sent successfully!");
        } catch (Exception e) {
            log.error("❌ Failed to send welcome email to: {}", user.getEmail(), e);
            // Don't fail if email sending fails
        }

        log.info("╔════════════════════════════════════════════════════════════════════════════╗");
        log.info("║         EMAIL VERIFICATION COMPLETED SUCCESSFULLY! 🎉                      ║");
        log.info("╚════════════════════════════════════════════════════════════════════════════╝");
    }

    @Transactional
    public TokenResponse refreshToken(String refreshToken) {
        // Validate refresh token
        if (!tokenProvider.validateToken(refreshToken)) {
            throw new UnauthorizedException("Invalid refresh token");
        }

        // Get email from token
        String email = tokenProvider.getEmailFromToken(refreshToken);

        // Find user and verify refresh token matches
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("User", "email", email));

        if (!refreshToken.equals(user.getRefreshToken())) {
            throw new UnauthorizedException("Invalid refresh token");
        }

        // Generate new tokens
        String newAccessToken = tokenProvider.generateAccessToken(email);
        String newRefreshToken = tokenProvider.generateRefreshToken(email);

        // Update refresh token
        user.setRefreshToken(newRefreshToken);
        userRepository.save(user);

        return TokenResponse.builder()
                .accessToken(newAccessToken)
                .refreshToken(newRefreshToken)
                .build();
    }

    @Transactional
    public void logout(String refreshToken) {
        if (refreshToken == null) {
            return;
        }

        try {
            String email = tokenProvider.getEmailFromToken(refreshToken);
            User user = userRepository.findByEmail(email)
                    .orElse(null);

            if (user != null) {
                // Clear refresh token
                user.setRefreshToken(null);
                userRepository.save(user);
            }
        } catch (Exception e) {
            // Token might be invalid, just ignore
        }
    }

    @Transactional
    public AuthResponse googleLogin(String idToken) {
        // TODO: Verify Google ID token
        // GoogleIdToken.Payload payload = verifyGoogleToken(idToken);
        // For now, throwing not implemented
        throw new BadRequestException("Google OAuth not implemented yet. Please configure Google OAuth2.");
    }

    @Transactional
    public AuthResponse githubLogin(String code) {
        // TODO: Exchange code for GitHub access token and get user info
        // For now, throwing not implemented
        throw new BadRequestException("GitHub OAuth not implemented yet. Please configure GitHub OAuth2.");
    }

    @Transactional
    public void forgotPassword(String email) {
        User user = userRepository.findByEmail(email)
                .orElse(null);

        // Don't reveal if email exists or not for security
        if (user == null) {
            return;
        }

        // Generate reset token
        String resetToken = UUID.randomUUID().toString();
        user.setPasswordResetToken(resetToken);
        user.setPasswordResetExpiry(LocalDateTime.now().plusHours(1));
        userRepository.save(user);

        // Send reset email
        emailService.sendPasswordResetEmail(user.getEmail(), resetToken, user.getFirstName());
    }

    @Transactional
    public void resetPassword(String token, String newPassword) {
        // Find user by reset token
        User user = userRepository.findByPasswordResetToken(token)
                .orElseThrow(() -> new BadRequestException("Invalid or expired reset token"));

        // Check if token expired
        if (user.getPasswordResetExpiry().isBefore(LocalDateTime.now())) {
            throw new BadRequestException("Reset token has expired");
        }

        // Update password
        user.setPassword(passwordEncoder.encode(newPassword));
        user.setPasswordResetToken(null);
        user.setPasswordResetExpiry(null);
        user.setRefreshToken(null); // Invalidate existing sessions
        userRepository.save(user);

        // Send confirmation email
        emailService.sendPasswordChangedEmail(user.getEmail(), user.getFirstName());
    }

}
