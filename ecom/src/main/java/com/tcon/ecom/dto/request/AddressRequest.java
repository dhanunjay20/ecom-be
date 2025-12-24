package com.tcon.ecom.dto.request;

import com.tcon.ecom.model.enums.AddressType;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class AddressRequest {

    private AddressType type;

    @NotBlank(message = "Full name is required")
    private String fullName;

    @NotBlank(message = "Street is required")
    private String street;

    private String apartment;

    @NotBlank(message = "City is required")
    private String city;

    @NotBlank(message = "State is required")
    private String state;

    @NotBlank(message = "ZIP code is required")
    private String zipCode;

    @NotBlank(message = "Country is required")
    private String country;

    @NotBlank(message = "Phone is required")
    private String phone;

    private Boolean isDefault = false;

    private String instructions;
}

