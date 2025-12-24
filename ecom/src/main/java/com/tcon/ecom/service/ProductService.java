package com.tcon.ecom.service;

import com.tcon.ecom.dto.response.ProductDetailResponse;
import com.tcon.ecom.dto.response.ProductListResponse;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

public interface ProductService {
    Map<String, Object> getProducts(
            String category, String subcategory, BigDecimal minPrice, BigDecimal maxPrice,
            List<String> colors, List<String> sizes, Boolean featured, Boolean newArrival,
            Boolean onSale, Boolean inStock, String sortBy, String search, int page, int limit
    );

    Map<String, Object> getProductBySlug(String slug, boolean includeRelated);
    List<ProductListResponse> getFeaturedProducts(int limit);
    List<ProductListResponse> getNewArrivals(int limit);
    Map<String, Object> getProductReviews(String productId, int page, int limit, String sortBy, Integer rating);
}

