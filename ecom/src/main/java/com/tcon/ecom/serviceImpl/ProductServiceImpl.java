package com.tcon.ecom.serviceImpl;

import com.tcon.ecom.dto.response.ProductDetailResponse;
import com.tcon.ecom.dto.response.ProductListResponse;
import com.tcon.ecom.exception.ResourceNotFoundException;
import com.tcon.ecom.model.Product;
import com.tcon.ecom.model.ProductReview;
import com.tcon.ecom.model.User;
import com.tcon.ecom.model.enums.ProductCategory;
import com.tcon.ecom.model.enums.ProductStatus;
import com.tcon.ecom.model.enums.ReviewStatus;
import com.tcon.ecom.repository.ProductRepository;
import com.tcon.ecom.repository.ProductReviewRepository;
import com.tcon.ecom.repository.UserRepository;
import com.tcon.ecom.service.ProductService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class ProductServiceImpl implements ProductService {

    private final ProductRepository productRepository;
    private final ProductReviewRepository productReviewRepository;
    private final UserRepository userRepository;
    private final MongoTemplate mongoTemplate;

    @Override
    public Map<String, Object> getProducts(
            String category, String subcategory, BigDecimal minPrice, BigDecimal maxPrice,
            List<String> colors, List<String> sizes, Boolean featured, Boolean newArrival,
            Boolean onSale, Boolean inStock, String sortBy, String search, int page, int limit) {

        Query query = new Query();
        Criteria criteria = Criteria.where("status").is(ProductStatus.ACTIVE);

        // Apply filters
        if (category != null) {
            criteria.and("category").is(ProductCategory.valueOf(category.toUpperCase()));
        }
        if (subcategory != null) {
            criteria.and("subcategory").is(subcategory);
        }
        if (minPrice != null) {
            criteria.and("price").gte(minPrice);
        }
        if (maxPrice != null) {
            criteria.and("price").lte(maxPrice);
        }
        if (featured != null && featured) {
            criteria.and("featured").is(true);
        }
        if (newArrival != null && newArrival) {
            criteria.and("newArrival").is(true);
        }
        if (onSale != null && onSale) {
            criteria.and("onSale").is(true);
        }
        if (inStock != null && inStock) {
            criteria.and("stock").gt(0);
        }
        if (search != null && !search.trim().isEmpty()) {
            criteria.orOperator(
                    Criteria.where("name").regex(search, "i"),
                    Criteria.where("description").regex(search, "i"),
                    Criteria.where("tags").regex(search, "i")
            );
        }

        query.addCriteria(criteria);

        // Apply sorting
        Sort sort = getSortOrder(sortBy);
        query.with(sort);

        // Get total count
        long total = mongoTemplate.count(query, Product.class);

        // Apply pagination
        query.with(PageRequest.of(page, limit));

        // Execute query
        List<Product> products = mongoTemplate.find(query, Product.class);

        // Build response
        Map<String, Object> response = new HashMap<>();
        response.put("products", products.stream()
                .map(this::mapToProductListResponse)
                .collect(Collectors.toList()));

        // Pagination info
        Map<String, Object> pagination = new HashMap<>();
        pagination.put("total", total);
        pagination.put("page", page + 1);
        pagination.put("pages", (int) Math.ceil((double) total / limit));
        pagination.put("limit", limit);
        response.put("pagination", pagination);

        // Available filters
        response.put("filters", getAvailableFilters(category));

        return response;
    }

    @Override
    public Map<String, Object> getProductBySlug(String slug, boolean includeRelated) {
        Product product = productRepository.findBySlug(slug)
                .orElseThrow(() -> new ResourceNotFoundException("Product not found"));

        Map<String, Object> response = new HashMap<>();
        response.put("product", mapToProductDetailResponse(product));

        if (includeRelated) {
            List<Product> relatedProducts = productRepository
                    .findByCategoryAndStatusAndIdNot(product.getCategory(), ProductStatus.ACTIVE, product.getId(),
                            PageRequest.of(0, 4))
                    .getContent();
            response.put("relatedProducts", relatedProducts.stream()
                    .map(this::mapToProductListResponse)
                    .collect(Collectors.toList()));
        }

        return response;
    }

    @Override
    public List<ProductListResponse> getFeaturedProducts(int limit) {
        Page<Product> products = productRepository.findByFeaturedTrueAndStatus(
                ProductStatus.ACTIVE, PageRequest.of(0, limit));
        return products.stream()
                .map(this::mapToProductListResponse)
                .collect(Collectors.toList());
    }

    @Override
    public List<ProductListResponse> getNewArrivals(int limit) {
        Page<Product> products = productRepository.findByNewArrivalTrueAndStatus(
                ProductStatus.ACTIVE, PageRequest.of(0, limit, Sort.by(Sort.Direction.DESC, "createdAt")));
        return products.stream()
                .map(this::mapToProductListResponse)
                .collect(Collectors.toList());
    }

    @Override
    public Map<String, Object> getProductReviews(String productId, int page, int limit, String sortBy, Integer rating) {
        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new ResourceNotFoundException("Product not found"));

        Pageable pageable = PageRequest.of(page, limit, getReviewSort(sortBy));
        Page<ProductReview> reviewsPage;

        if (rating != null) {
            reviewsPage = productReviewRepository.findByProductIdAndStatusAndRating(
                    productId, ReviewStatus.APPROVED, rating, pageable);
        } else {
            reviewsPage = productReviewRepository.findByProductIdAndStatus(
                    productId, ReviewStatus.APPROVED, pageable);
        }

        Map<String, Object> response = new HashMap<>();
        response.put("reviews", reviewsPage.getContent());

        Map<String, Object> pagination = new HashMap<>();
        pagination.put("total", reviewsPage.getTotalElements());
        pagination.put("page", page + 1);
        pagination.put("pages", reviewsPage.getTotalPages());
        response.put("pagination", pagination);

        // Rating summary
        Map<String, Object> summary = new HashMap<>();
        summary.put("averageRating", product.getAverageRating());
        summary.put("totalReviews", product.getReviewCount());
        summary.put("ratingDistribution", getRatingDistribution(productId));
        response.put("summary", summary);

        return response;
    }

    private Sort getSortOrder(String sortBy) {
        return switch (sortBy != null ? sortBy : "newest") {
            case "price-low" -> Sort.by(Sort.Direction.ASC, "price");
            case "price-high" -> Sort.by(Sort.Direction.DESC, "price");
            case "rating" -> Sort.by(Sort.Direction.DESC, "averageRating");
            case "popular" -> Sort.by(Sort.Direction.DESC, "reviewCount");
            default -> Sort.by(Sort.Direction.DESC, "createdAt");
        };
    }

    private Sort getReviewSort(String sortBy) {
        return switch (sortBy != null ? sortBy : "recent") {
            case "helpful" -> Sort.by(Sort.Direction.DESC, "helpfulCount");
            case "rating-high" -> Sort.by(Sort.Direction.DESC, "rating");
            case "rating-low" -> Sort.by(Sort.Direction.ASC, "rating");
            default -> Sort.by(Sort.Direction.DESC, "createdAt");
        };
    }

    private Map<String, Object> getAvailableFilters(String category) {
        Map<String, Object> filters = new HashMap<>();

        // Get distinct subcategories
        Query query = new Query(Criteria.where("status").is(ProductStatus.ACTIVE));
        if (category != null) {
            query.addCriteria(Criteria.where("category").is(ProductCategory.valueOf(category.toUpperCase())));
        }

        List<Product> products = mongoTemplate.find(query, Product.class);

        filters.put("availableSubcategories", products.stream()
                .map(Product::getSubcategory)
                .filter(Objects::nonNull)
                .distinct()
                .collect(Collectors.toList()));

        // Price range
        if (!products.isEmpty()) {
            BigDecimal minPrice = products.stream()
                    .map(Product::getPrice)
                    .min(BigDecimal::compareTo)
                    .orElse(BigDecimal.ZERO);
            BigDecimal maxPrice = products.stream()
                    .map(Product::getPrice)
                    .max(BigDecimal::compareTo)
                    .orElse(BigDecimal.ZERO);
            filters.put("priceRange", Map.of("min", minPrice, "max", maxPrice));
        }

        return filters;
    }

    private Map<Integer, Long> getRatingDistribution(String productId) {
        Map<Integer, Long> distribution = new HashMap<>();
        for (int i = 1; i <= 5; i++) {
            Long count = productReviewRepository.countByProductIdAndStatusAndRating(
                    productId, ReviewStatus.APPROVED, i);
            distribution.put(i, count);
        }
        return distribution;
    }

    private ProductListResponse mapToProductListResponse(Product product) {
        ProductListResponse response = new ProductListResponse();
        response.setId(product.getId());
        response.setName(product.getName());
        response.setSlug(product.getSlug());
        response.setDescription(product.getShortDescription());
        response.setPrice(product.getPrice());
        response.setComparePrice(product.getComparePrice());
        response.setCategory(product.getCategory().name());
        response.setSubcategory(product.getSubcategory());
        response.setImages(product.getImages().stream()
                .map(img -> img.getUrl())
                .collect(Collectors.toList()));
        response.setInStock(product.getStock() > 0);
        response.setFeatured(product.getFeatured());
        response.setNewArrival(product.getNewArrival());
        response.setAverageRating(product.getAverageRating());
        response.setReviewCount(product.getReviewCount());
        return response;
    }

    private ProductDetailResponse mapToProductDetailResponse(Product product) {
        ProductDetailResponse response = new ProductDetailResponse();
        response.setId(product.getId());
        response.setName(product.getName());
        response.setSlug(product.getSlug());
        response.setDescription(product.getDescription());
        response.setPrice(product.getPrice());
        response.setComparePrice(product.getComparePrice());
        response.setCategory(product.getCategory().name());
        response.setSubcategory(product.getSubcategory());
        response.setTags(product.getTags());
        response.setImages(product.getImages().stream()
                .map(img -> {
                    ProductDetailResponse.ProductImage imgResponse = new ProductDetailResponse.ProductImage();
                    imgResponse.setUrl(img.getUrl());
                    imgResponse.setAltText(img.getAltText());
                    imgResponse.setIsPrimary(img.getIsPrimary());
                    return imgResponse;
                })
                .collect(Collectors.toList()));

        // Convert ProductVariants to Map
        if (product.getVariants() != null) {
            Map<String, Object> variantsMap = new HashMap<>();
            variantsMap.put("colors", product.getVariants().getColors());
            variantsMap.put("sizes", product.getVariants().getSizes());
            variantsMap.put("materials", product.getVariants().getMaterials());
            response.setVariants(variantsMap);
        }

        response.setStock(product.getStock());
        response.setFeatured(product.getFeatured());
        response.setNewArrival(product.getNewArrival());
        response.setOnSale(product.getOnSale());
        response.setAverageRating(product.getAverageRating());
        response.setReviewCount(product.getReviewCount());

        // Vendor info
        if (product.getVendorId() != null) {
            userRepository.findById(product.getVendorId()).ifPresent(vendor -> {
                ProductDetailResponse.VendorInfo vendorInfo = new ProductDetailResponse.VendorInfo();
                vendorInfo.setId(vendor.getId());
                vendorInfo.setName(vendor.getFirstName() + " " + vendor.getLastName());
                response.setVendor(vendorInfo);
            });
        }

        return response;
    }
}

