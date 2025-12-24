package com.tcon.ecom.dto.response;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class UserResponse {
    private String id;
    private String email;
    private String firstName;
    private String lastName;
    private String phone;
    private String avatar;
    private String role;
    private Integer loyaltyPoints;
    private Object preferences;
    private Boolean emailVerified;
    private LocalDateTime memberSince;
    private LocalDateTime createdAt;
}

