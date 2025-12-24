package com.tcon.ecom.dto.response;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ProductDetailResponse {
    private String id;
    private String name;
    private String slug;
    private String description;
    private BigDecimal price;
    private BigDecimal comparePrice;
    private String category;
    private String subcategory;
    private List<String> tags;
    private List<ProductImage> images;
    private Map<String, Object> variants;
    private Integer stock;
    private Boolean featured;
    private Boolean newArrival;
    private Boolean onSale;
    private Double averageRating;
    private Integer reviewCount;
    private VendorInfo vendor;

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ProductImage {
        private String url;
        private String altText;
        private Boolean isPrimary;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class VendorInfo {
        private String id;
        private String name;
    }
}

