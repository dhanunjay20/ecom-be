package com.tcon.ecom.dto.response;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CartResponse {
    private String id;
    private List<CartItemResponse> items;
    private BigDecimal subtotal;
    private Integer itemCount;
    private LocalDateTime updatedAt;

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class CartItemResponse {
        private String id;
        private String productId;
        private ProductInfo product;
        private Integer quantity;
        private String selectedColor;
        private String selectedSize;
        private BigDecimal price;
        private BigDecimal subtotal;
        private LocalDateTime addedAt;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ProductInfo {
        private String id;
        private String name;
        private String slug;
        private BigDecimal price;
        private List<String> images;
        private Integer stock;
        private String status;
        private Boolean inStock;
    }
}

