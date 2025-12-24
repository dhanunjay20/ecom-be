package com.tcon.ecom.dto.response;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class AddressResponse {
    private String id;
    private String type;
    private Boolean isDefault;
    private String fullName;
    private String street;
    private String apartment;
    private String city;
    private String state;
    private String zipCode;
    private String country;
    private String phone;
    private String instructions;
    private LocalDateTime createdAt;
}

