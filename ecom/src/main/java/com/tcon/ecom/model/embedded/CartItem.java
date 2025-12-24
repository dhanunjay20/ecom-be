package com.tcon.ecom.model.embedded;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CartItem {
    private String productId;
    private Integer quantity;
    private String selectedColor;
    private String selectedSize;
    private BigDecimal price;
    private LocalDateTime addedAt;
}

