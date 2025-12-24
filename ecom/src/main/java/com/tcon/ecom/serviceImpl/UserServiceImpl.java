package com.tcon.ecom.serviceImpl;

import com.tcon.ecom.dto.request.UpdatePasswordRequest;
import com.tcon.ecom.dto.request.UpdatePreferencesRequest;
import com.tcon.ecom.dto.request.UpdateProfileRequest;
import com.tcon.ecom.dto.response.UserResponse;
import com.tcon.ecom.exception.BadRequestException;
import com.tcon.ecom.exception.ResourceNotFoundException;
import com.tcon.ecom.model.User;
import com.tcon.ecom.model.embedded.NotificationPreferences;
import com.tcon.ecom.model.embedded.UserPreferences;
import com.tcon.ecom.repository.UserRepository;
import com.tcon.ecom.service.UserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.modelmapper.ModelMapper;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

@Service
@RequiredArgsConstructor
@Slf4j
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final ModelMapper modelMapper;
    // TODO: Add FileStorageService for avatar upload

    @Override
    public UserResponse getCurrentUser(String email) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        return mapToUserResponse(user);
    }

    @Override
    @Transactional
    public UserResponse updateProfile(String email, UpdateProfileRequest request) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        if (request.getFirstName() != null) {
            user.setFirstName(request.getFirstName());
        }
        if (request.getLastName() != null) {
            user.setLastName(request.getLastName());
        }
        if (request.getPhone() != null) {
            user.setPhone(request.getPhone());
        }
        if (request.getAvatar() != null) {
            user.setAvatar(request.getAvatar());
        }

        User updatedUser = userRepository.save(user);
        log.info("Profile updated for user: {}", email);

        return mapToUserResponse(updatedUser);
    }

    @Override
    @Transactional
    public void updatePassword(String email, UpdatePasswordRequest request) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        // Verify current password
        if (!passwordEncoder.matches(request.getCurrentPassword(), user.getPassword())) {
            throw new BadRequestException("Current password is incorrect");
        }

        // Validate new password matches confirm password
        if (!request.getNewPassword().equals(request.getConfirmPassword())) {
            throw new BadRequestException("New password and confirm password do not match");
        }

        // Update password
        user.setPassword(passwordEncoder.encode(request.getNewPassword()));
        userRepository.save(user);

        log.info("Password updated for user: {}", email);

        // TODO: Invalidate all sessions except current
        // TODO: Send password change notification email
    }

    @Override
    @Transactional
    public Object updatePreferences(String email, UpdatePreferencesRequest request) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        UserPreferences preferences = user.getPreferences();
        if (preferences == null) {
            preferences = new UserPreferences();
        }

        if (request.getLanguage() != null) {
            preferences.setLanguage(request.getLanguage());
        }
        if (request.getCurrency() != null) {
            preferences.setCurrency(request.getCurrency());
        }
        if (request.getNotifications() != null) {
            NotificationPreferences notifPref = preferences.getNotifications();
            if (notifPref == null) {
                notifPref = new NotificationPreferences();
            }

            if (request.getNotifications().getOrderUpdates() != null) {
                notifPref.setOrderUpdates(request.getNotifications().getOrderUpdates());
            }
            if (request.getNotifications().getPromotions() != null) {
                notifPref.setPromotions(request.getNotifications().getPromotions());
            }
            if (request.getNotifications().getNewsletter() != null) {
                notifPref.setNewsletter(request.getNotifications().getNewsletter());
            }
            if (request.getNotifications().getProductUpdates() != null) {
                notifPref.setProductUpdates(request.getNotifications().getProductUpdates());
            }

            preferences.setNotifications(notifPref);
        }

        user.setPreferences(preferences);
        userRepository.save(user);

        log.info("Preferences updated for user: {}", email);

        return preferences;
    }

    @Override
    @Transactional
    public String uploadAvatar(String email, MultipartFile file) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        // Validate file
        if (file.isEmpty()) {
            throw new BadRequestException("File is empty");
        }

        // Validate file type
        String contentType = file.getContentType();
        if (contentType == null || !contentType.startsWith("image/")) {
            throw new BadRequestException("Only image files are allowed");
        }

        // Validate file size (5MB)
        if (file.getSize() > 5 * 1024 * 1024) {
            throw new BadRequestException("File size exceeds 5MB limit");
        }

        // TODO: Upload to S3/Cloudinary
        // String avatarUrl = fileStorageService.uploadAvatar(file, user.getId());

        // For now, using a placeholder
        String avatarUrl = "https://example.com/avatars/" + user.getId() + ".jpg";

        // Delete old avatar if exists
        // if (user.getAvatar() != null) {
        //     fileStorageService.deleteFile(user.getAvatar());
        // }

        user.setAvatar(avatarUrl);
        userRepository.save(user);

        log.info("Avatar uploaded for user: {}", email);

        return avatarUrl;
    }

    private UserResponse mapToUserResponse(User user) {
        UserResponse response = new UserResponse();
        response.setId(user.getId());
        response.setEmail(user.getEmail());
        response.setFirstName(user.getFirstName());
        response.setLastName(user.getLastName());
        response.setPhone(user.getPhone());
        response.setAvatar(user.getAvatar());
        response.setRole(user.getRole().name());
        response.setLoyaltyPoints(user.getLoyaltyPoints());
        response.setPreferences(user.getPreferences());
        response.setEmailVerified(user.getEmailVerified());
        response.setMemberSince(user.getCreatedAt());
        response.setCreatedAt(user.getCreatedAt());
        return response;
    }
}

