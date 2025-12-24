package com.tcon.ecom.repository;

import com.tcon.ecom.model.Product;
import com.tcon.ecom.model.enums.ProductCategory;
import com.tcon.ecom.model.enums.ProductStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ProductRepository extends MongoRepository<Product, String> {

    Optional<Product> findBySlug(String slug);

    Optional<Product> findBySku(String sku);

    Page<Product> findByStatus(ProductStatus status, Pageable pageable);

    Page<Product> findByVendorId(String vendorId, Pageable pageable);

    Page<Product> findByVendorIdAndStatus(String vendorId, ProductStatus status, Pageable pageable);

    Page<Product> findByCategoryAndStatus(ProductCategory category, ProductStatus status, Pageable pageable);

    Page<Product> findByFeaturedTrueAndStatus(ProductStatus status, Pageable pageable);

    Page<Product> findByNewArrivalTrueAndStatus(ProductStatus status, Pageable pageable);

    Page<Product> findByOnSaleTrueAndStatus(ProductStatus status, Pageable pageable);

    @Query("{ 'name': { $regex: ?0, $options: 'i' }, 'status': ?1 }")
    Page<Product> searchByName(String keyword, ProductStatus status, Pageable pageable);

    @Query("{ 'tags': ?0, 'status': ?1 }")
    Page<Product> findByTagsContainingAndStatus(String tag, ProductStatus status, Pageable pageable);

    Page<Product> findByCategoryAndStatusAndIdNot(ProductCategory category, ProductStatus status, String id, Pageable pageable);
    Boolean existsBySlug(String slug);

    Boolean existsBySku(String sku);

    Long countByVendorId(String vendorId);

    Long countByStatus(ProductStatus status);

    Long countByCategoryAndStatus(ProductCategory category, ProductStatus status);
}

