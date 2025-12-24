package com.tcon.ecom.model.embedded;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class BillingAddress {
    private String fullName;
    private String street;
    private String city;
    private String state;
    private String zipCode;
    private String country = "United States";
}

