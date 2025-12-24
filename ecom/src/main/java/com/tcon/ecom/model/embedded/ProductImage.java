package com.tcon.ecom.model.embedded;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ProductImage {
    private String url;
    private String altText;
    private Integer order;
    private Boolean isPrimary = false;
}

