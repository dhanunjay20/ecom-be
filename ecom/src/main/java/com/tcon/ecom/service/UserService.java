package com.tcon.ecom.service;

import com.tcon.ecom.dto.request.UpdatePasswordRequest;
import com.tcon.ecom.dto.request.UpdatePreferencesRequest;
import com.tcon.ecom.dto.request.UpdateProfileRequest;
import com.tcon.ecom.dto.response.UserResponse;
import org.springframework.web.multipart.MultipartFile;

public interface UserService {
    UserResponse getCurrentUser(String email);
    UserResponse updateProfile(String email, UpdateProfileRequest request);
    void updatePassword(String email, UpdatePasswordRequest request);
    Object updatePreferences(String email, UpdatePreferencesRequest request);
    String uploadAvatar(String email, MultipartFile file);
}

