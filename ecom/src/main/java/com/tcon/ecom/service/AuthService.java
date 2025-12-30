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
import com.tcon.ecom.repository.UserRepository;
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
        User user = userRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new BadCredentialsException("Invalid email or password"));

        // Check if account is locked
        if (!user.isAccountNonLocked()) {
            throw new UnauthorizedException("Account is temporarily locked. Please try again later.");
        }

        // Check if account is active or pending (allow login for pending users)
        if (user.getStatus() != UserStatus.ACTIVE && user.getStatus() != UserStatus.PENDING) {
            throw new UnauthorizedException("Account is " + user.getStatus().name().toLowerCase());
        }

        try {
            // Authenticate
            Authentication authentication = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(request.getEmail(), request.getPassword())
            );

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

            // Build response
            UserResponse userResponse = modelMapper.map(user, UserResponse.class);
            userResponse.setMemberSince(user.getCreatedAt());

            return AuthResponse.builder()
                    .user(userResponse)
                    .accessToken(accessToken)
                    .refreshToken(refreshToken)
                    .isNewUser(false)
                    .build();

        } catch (BadCredentialsException e) {
            // Increment login attempts
            user.setLoginAttempts(user.getLoginAttempts() + 1);

            // Lock account after 3 failed attempts for 15 minutes
            if (user.getLoginAttempts() >= 3) {
                user.setLockUntil(LocalDateTime.now().plusMinutes(15));
                userRepository.save(user);
                throw new UnauthorizedException("Too many failed login attempts. Account locked for 15 minutes.");
            }

            userRepository.save(user);
            throw new BadCredentialsException("Invalid email or password");
        }
    }

    @Transactional
    public void verifyEmail(String token) {
        User user = userRepository.findByEmailVerificationToken(token)
                .orElseThrow(() -> new BadRequestException("Invalid or expired verification token"));

        // Check if token expired
        if (user.getEmailVerificationExpiry().isBefore(LocalDateTime.now())) {
            throw new BadRequestException("Verification token has expired");
        }

        // Verify email
        user.setEmailVerified(true);
        user.setEmailVerificationToken(null);
        user.setEmailVerificationExpiry(null);
        userRepository.save(user);

        // Send welcome email
        emailService.sendWelcomeEmail(user.getEmail(), user.getFirstName());
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
