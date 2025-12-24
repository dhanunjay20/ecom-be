package com.tcon.ecom.dto.request;

import com.tcon.ecom.model.enums.ProductCategory;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CreateProductRequest {

    @NotBlank(message = "Product name is required")
    private String name;

    @NotBlank(message = "Description is required")
    private String description;

    private String shortDescription;

    @NotNull(message = "Category is required")
    private ProductCategory category;

    private String subcategory;

    @NotNull(message = "Price is required")
    private BigDecimal price;

    private BigDecimal comparePrice;

    @NotNull(message = "Cost is required")
    private BigDecimal cost;

    @NotBlank(message = "SKU is required")
    private String sku;

    @NotNull(message = "Stock is required")
    private Integer stock;

    private Integer lowStockThreshold;

    private Map<String, Object> variants; // colors, sizes, materials

    private List<String> tags;

    private String status; // draft or active

    private Boolean featured = false;
    private Boolean newArrival = false;
    private Boolean onSale = false;

    private String metaTitle;
    private String metaDescription;
}

