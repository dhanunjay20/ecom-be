package com.tcon.ecom.model.embedded;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ShippingAddress {
    private String fullName;
    private String street;
    private String apartment;
    private String city;
    private String state;
    private String zipCode;
    private String country = "United States";
    private String phone;
}

