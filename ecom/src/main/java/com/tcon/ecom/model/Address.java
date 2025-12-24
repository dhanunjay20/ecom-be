package com.tcon.ecom.model;

import com.tcon.ecom.model.enums.AddressType;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.mongodb.core.index.CompoundIndex;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDateTime;

@Document(collection = "addresses")
@Data
@NoArgsConstructor
@AllArgsConstructor
@CompoundIndex(name = "userId_isDefault", def = "{'userId': 1, 'isDefault': 1}")
public class Address {

    @Id
    private String id;

    @NotBlank
    @Indexed
    private String userId;

    private AddressType type = AddressType.HOME;
    private Boolean isDefault = false;

    // Address Details
    @NotBlank
    private String fullName;

    @NotBlank
    private String street;

    private String apartment;

    @NotBlank
    private String city;

    @NotBlank
    private String state;

    @NotBlank
    private String zipCode;

    private String country = "United States";

    @NotBlank
    private String phone;

    // Additional
    private String instructions;

    // Timestamps
    @CreatedDate
    private LocalDateTime createdAt;

    @LastModifiedDate
    private LocalDateTime updatedAt;
}

