package com.tcon.ecom.model.embedded;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class OrderItem {
    private String productId;
    private String vendorId;
    private String name;
    private String sku;
    private Integer quantity;
    private BigDecimal price;
    private String selectedColor;
    private String selectedSize;
    private String image;
}

