package com.tcon.ecom.controller;

import com.tcon.ecom.dto.response.ApiResponse;
import com.tcon.ecom.dto.response.ProductDetailResponse;
import com.tcon.ecom.dto.response.ProductListResponse;
import com.tcon.ecom.dto.response.ProductReviewResponse;
import com.tcon.ecom.service.ProductService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/products")
@Tag(name = "Products", description = "Product catalog and browsing APIs")
@RequiredArgsConstructor
public class ProductController {

    private final ProductService productService;

    @GetMapping
    @Operation(summary = "Get products with filters and pagination")
    public ResponseEntity<ApiResponse<Map<String, Object>>> getProducts(
            @RequestParam(required = false) String category,
            @RequestParam(required = false) String subcategory,
            @RequestParam(required = false) BigDecimal minPrice,
            @RequestParam(required = false) BigDecimal maxPrice,
            @RequestParam(required = false) List<String> colors,
            @RequestParam(required = false) List<String> sizes,
            @RequestParam(required = false) Boolean featured,
            @RequestParam(required = false) Boolean newArrival,
            @RequestParam(required = false) Boolean onSale,
            @RequestParam(defaultValue = "true") Boolean inStock,
            @RequestParam(defaultValue = "newest") String sortBy,
            @RequestParam(required = false) String search,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "20") int limit) {

        Map<String, Object> result = productService.getProducts(
                category, subcategory, minPrice, maxPrice, colors, sizes,
                featured, newArrival, onSale, inStock, sortBy, search,
                page - 1, limit
        );
        return ResponseEntity.ok(ApiResponse.success(result));
    }

    @GetMapping("/{slug}")
    @Operation(summary = "Get product details by slug")
    public ResponseEntity<ApiResponse<Map<String, Object>>> getProductBySlug(
            @PathVariable String slug,
            @RequestParam(defaultValue = "false") boolean includeRelated) {
        Map<String, Object> result = productService.getProductBySlug(slug, includeRelated);
        return ResponseEntity.ok(ApiResponse.success(result));
    }

    @GetMapping("/featured")
    @Operation(summary = "Get featured products")
    public ResponseEntity<ApiResponse<List<ProductListResponse>>> getFeaturedProducts(
            @RequestParam(defaultValue = "8") int limit) {
        List<ProductListResponse> products = productService.getFeaturedProducts(limit);
        return ResponseEntity.ok(ApiResponse.success(products));
    }

    @GetMapping("/new-arrivals")
    @Operation(summary = "Get new arrival products")
    public ResponseEntity<ApiResponse<List<ProductListResponse>>> getNewArrivals(
            @RequestParam(defaultValue = "8") int limit) {
        List<ProductListResponse> products = productService.getNewArrivals(limit);
        return ResponseEntity.ok(ApiResponse.success(products));
    }

    @GetMapping("/{productId}/reviews")
    @Operation(summary = "Get product reviews")
    public ResponseEntity<ApiResponse<Map<String, Object>>> getProductReviews(
            @PathVariable String productId,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int limit,
            @RequestParam(defaultValue = "recent") String sortBy,
            @RequestParam(required = false) Integer rating) {
        Map<String, Object> result = productService.getProductReviews(productId, page - 1, limit, sortBy, rating);
        return ResponseEntity.ok(ApiResponse.success(result));
    }
}

