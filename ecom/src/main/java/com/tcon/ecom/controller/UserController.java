package com.tcon.ecom.controller;

import com.tcon.ecom.dto.request.UpdatePasswordRequest;
import com.tcon.ecom.dto.request.UpdatePreferencesRequest;
import com.tcon.ecom.dto.request.UpdateProfileRequest;
import com.tcon.ecom.dto.response.ApiResponse;
import com.tcon.ecom.dto.response.UserResponse;
import com.tcon.ecom.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/api/users")
@Tag(name = "User Management", description = "User profile and settings APIs")
@SecurityRequirement(name = "Bearer Authentication")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;

    @GetMapping("/me")
    @Operation(summary = "Get current user profile")
    public ResponseEntity<ApiResponse<UserResponse>> getCurrentUser(
            @AuthenticationPrincipal UserDetails userDetails) {
        UserResponse user = userService.getCurrentUser(userDetails.getUsername());
        return ResponseEntity.ok(ApiResponse.success(user));
    }

    @PatchMapping("/me")
    @Operation(summary = "Update user profile")
    public ResponseEntity<ApiResponse<UserResponse>> updateProfile(
            @AuthenticationPrincipal UserDetails userDetails,
            @Valid @RequestBody UpdateProfileRequest request) {
        UserResponse user = userService.updateProfile(userDetails.getUsername(), request);
        return ResponseEntity.ok(ApiResponse.success("Profile updated successfully", user));
    }

    @PatchMapping("/me/password")
    @Operation(summary = "Update password")
    public ResponseEntity<ApiResponse<String>> updatePassword(
            @AuthenticationPrincipal UserDetails userDetails,
            @Valid @RequestBody UpdatePasswordRequest request) {
        userService.updatePassword(userDetails.getUsername(), request);
        return ResponseEntity.ok(ApiResponse.success("Password updated successfully", null));
    }

    @PatchMapping("/me/preferences")
    @Operation(summary = "Update user preferences")
    public ResponseEntity<ApiResponse<Object>> updatePreferences(
            @AuthenticationPrincipal UserDetails userDetails,
            @Valid @RequestBody UpdatePreferencesRequest request) {
        Object preferences = userService.updatePreferences(userDetails.getUsername(), request);
        return ResponseEntity.ok(ApiResponse.success("Preferences updated", preferences));
    }

    @PostMapping("/me/avatar")
    @Operation(summary = "Upload user avatar")
    public ResponseEntity<ApiResponse<String>> uploadAvatar(
            @AuthenticationPrincipal UserDetails userDetails,
            @RequestParam("avatar") MultipartFile file) {
        String avatarUrl = userService.uploadAvatar(userDetails.getUsername(), file);
        return ResponseEntity.ok(ApiResponse.success("Avatar uploaded successfully", avatarUrl));
    }
}

