package com.tcon.ecom.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class VendorResponse {
    private String id;
    private String userId;
    private String email;
    private String status;
    private String storeName;
    private String contactPerson;
    private String phone;
    private String businessType;
    private String taxId;
    private BusinessAddress businessAddress;
    private String logoUrl;
    private String description;
    private String website;
    private LocalDateTime createdAt;
    private LocalDateTime approvedAt;
    private String rejectionReason;

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class BusinessAddress {
        private String street;
        private String city;
        private String state;
        private String zipCode;
        private String country;
    }
}

