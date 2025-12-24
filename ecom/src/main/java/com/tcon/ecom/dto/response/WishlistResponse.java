package com.tcon.ecom.dto.response;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class WishlistResponse {
    private String id;
    private ProductInfo product;
    private BigDecimal priceAtAdd;
    private Boolean priceDropped;
    private LocalDateTime addedAt;

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ProductInfo {
        private String id;
        private String name;
        private String slug;
        private BigDecimal price;
        private BigDecimal comparePrice;
        private String primaryImage;
        private String category;
        private String subcategory;
        private Boolean inStock;
        private Double averageRating;
    }
}

