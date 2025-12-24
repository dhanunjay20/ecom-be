package com.tcon.ecom.model;

import com.tcon.ecom.model.embedded.ProductDimensions;
import com.tcon.ecom.model.embedded.ProductImage;
import com.tcon.ecom.model.embedded.ProductVariants;
import com.tcon.ecom.model.enums.ProductCategory;
import com.tcon.ecom.model.enums.ProductStatus;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.index.TextIndexed;
import org.springframework.data.mongodb.core.mapping.Document;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Document(collection = "products")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Product {

    @Id
    private String id;

    @NotBlank
    @Indexed
    private String vendorId;

    // Basic Information
    @NotBlank
    @TextIndexed
    private String name;

    @NotBlank
    @Indexed(unique = true)
    private String slug;

    @TextIndexed(weight = 2)
    private String description;

    private String shortDescription;

    @NotBlank
    @Indexed(unique = true)
    private String sku;

    // Categorization
    @NotNull
    @Indexed
    private ProductCategory category;

    private String subcategory;

    @Indexed
    private List<String> tags;

    // Pricing
    @NotNull
    private BigDecimal price;

    private BigDecimal comparePrice; // Original price
    private BigDecimal cost; // Vendor's cost, private

    // Inventory
    private Integer stock = 0;
    private Integer lowStockThreshold = 10;
    private Boolean trackInventory = true;
    private Boolean allowBackorder = false;

    // Variants
    private ProductVariants variants;

    // Media
    private List<ProductImage> images;

    // SEO & Marketing
    @Indexed
    private Boolean featured = false;

    @Indexed
    private Boolean newArrival = false;

    @Indexed
    private Boolean onSale = false;

    private String metaTitle;
    private String metaDescription;

    // Reviews & Ratings
    @Indexed
    private Double averageRating = 0.0;

    private Integer reviewCount = 0;

    // Dimensions & Shipping
    private Double weight; // in grams/ounces
    private ProductDimensions dimensions;

    // Status & Visibility
    @Indexed
    private ProductStatus status = ProductStatus.DRAFT;

    private LocalDateTime publishedAt;

    // Timestamps
    @CreatedDate
    @Indexed
    private LocalDateTime createdAt;

    @LastModifiedDate
    private LocalDateTime updatedAt;

    private LocalDateTime deletedAt;
}

