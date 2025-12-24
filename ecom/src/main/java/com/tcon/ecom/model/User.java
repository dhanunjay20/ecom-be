package com.tcon.ecom.model;

import com.tcon.ecom.model.embedded.UserPreferences;
import com.tcon.ecom.model.enums.UserRole;
import com.tcon.ecom.model.enums.UserStatus;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDateTime;

@Document(collection = "users")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class User {

    @Id
    private String id;

    @NotBlank
    @Email
    @Indexed(unique = true)
    private String email;

    @NotBlank
    private String password;

    @Indexed
    private UserRole role = UserRole.CUSTOMER;

    // Profile Information
    @NotBlank
    private String firstName;

    @NotBlank
    private String lastName;

    private String phone;
    private String avatar;

    // Authentication & Security
    @Indexed
    private Boolean emailVerified = false;

    private String emailVerificationToken;
    private LocalDateTime emailVerificationExpiry;
    private String passwordResetToken;
    private LocalDateTime passwordResetExpiry;
    private String refreshToken;
    private LocalDateTime lastLogin;
    private Integer loginAttempts = 0;
    private LocalDateTime lockUntil;

    // Social Auth
    @Indexed(unique = true, sparse = true)
    private String googleId;

    @Indexed(unique = true, sparse = true)
    private String facebookId;

    @Indexed(unique = true, sparse = true)
    private String githubId;

    // Customer-specific
    private Integer loyaltyPoints = 0;

    // Vendor-specific
    private String vendorProfile; // Reference to vendor profile if needed

    // Preferences
    private UserPreferences preferences = new UserPreferences();

    // Status & Tracking
    @Indexed
    private UserStatus status = UserStatus.ACTIVE;

    @CreatedDate
    @Indexed
    private LocalDateTime createdAt;

    @LastModifiedDate
    private LocalDateTime updatedAt;

    private LocalDateTime deletedAt; // Soft delete

    // Helper methods
    public String getFullName() {
        return firstName + " " + lastName;
    }

    public boolean isAccountNonLocked() {
        return lockUntil == null || LocalDateTime.now().isAfter(lockUntil);
    }
}

