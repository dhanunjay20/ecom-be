package com.tcon.ecom.dto.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class VendorRegisterRequest {

    @NotBlank(message = "Email is required")
    @Email(message = "Invalid email format")
    private String email;

    @NotBlank(message = "Password is required")
    private String password;

    @NotBlank(message = "Store name is required")
    private String storeName;

    @NotBlank(message = "Contact person is required")
    private String contactPerson;

    @NotBlank(message = "Phone is required")
    private String phone;

    @NotBlank(message = "Business type is required")
    private String businessType;

    @NotBlank(message = "Tax ID is required")
    private String taxId;

    private BusinessAddress businessAddress;

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class BusinessAddress {
        private String street;
        private String city;
        private String state;
        private String zipCode;
        private String country;
    }
}

