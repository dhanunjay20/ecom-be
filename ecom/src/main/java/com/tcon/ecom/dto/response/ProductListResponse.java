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
public class ProductListResponse {
    private String id;
    private String name;
    private String slug;
    private String description;
    private BigDecimal price;
    private BigDecimal comparePrice;
    private String category;
    private String subcategory;
    private List<String> images;
    private List<String> colors;
    private List<String> sizes;
    private Boolean inStock;
    private Boolean featured;
    private Boolean newArrival;
    private Double averageRating;
    private Integer reviewCount;
}

