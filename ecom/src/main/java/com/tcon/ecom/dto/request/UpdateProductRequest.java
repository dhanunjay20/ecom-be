package com.tcon.ecom.dto.request;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class UpdateProductRequest {
    private String name;
    private String description;
    private String shortDescription;
    private String category;
    private String subcategory;
    private BigDecimal price;
    private BigDecimal comparePrice;
    private BigDecimal cost;
    private String sku;
    private Integer stock;
    private Integer lowStockThreshold;
    private Map<String, Object> variants;
    private List<String> tags;
    private String status;
    private Boolean featured;
    private Boolean newArrival;
    private Boolean onSale;
    private String metaTitle;
    private String metaDescription;
}

