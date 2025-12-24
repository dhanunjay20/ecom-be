package com.tcon.ecom.model.embedded;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ProductDimensions {
    private Double length;
    private Double width;
    private Double height;
    private String unit = "cm"; // cm or inch
}

