package com.tcon.ecom.model;

import com.tcon.ecom.model.enums.VendorStatus;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDateTime;

@Document(collection = "vendors")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Vendor {

    @Id
    private String id;

    @Indexed(unique = true)
    private String userId; // Reference to User document

    @Indexed(unique = true)
    private String email;

    private String storeName;
    private String contactPerson;
    private String phone;
    private String businessType;
    private String taxId;

    // Business Address
    private BusinessAddress businessAddress;

    @Indexed
    private VendorStatus status = VendorStatus.PENDING; // PENDING, APPROVED, REJECTED, SUSPENDED

    // Additional fields
    private String logoUrl;
    private String description;
    private String website;
    private String bankAccountNumber;
    private String bankName;
    private String ifscCode;

    // Tracking
    @CreatedDate
    @Indexed
    private LocalDateTime createdAt;

    @LastModifiedDate
    private LocalDateTime updatedAt;

    private LocalDateTime approvedAt;
    private String approvedBy;
    private String rejectionReason;

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

