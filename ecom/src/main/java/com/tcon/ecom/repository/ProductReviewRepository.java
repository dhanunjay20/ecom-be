package com.tcon.ecom.repository;

import com.tcon.ecom.model.ProductReview;
import com.tcon.ecom.model.enums.ReviewStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ProductReviewRepository extends MongoRepository<ProductReview, String> {

    Page<ProductReview> findByProductIdAndStatus(String productId, ReviewStatus status, Pageable pageable);

    List<ProductReview> findByProductIdAndStatus(String productId, ReviewStatus status);

    Page<ProductReview> findByUserId(String userId, Pageable pageable);

    Optional<ProductReview> findByProductIdAndUserId(String productId, String userId);

    Boolean existsByProductIdAndUserId(String productId, String userId);

    Boolean existsByOrderIdAndProductId(String orderId, String productId);

    Long countByProductIdAndStatus(String productId, ReviewStatus status);

    Long countByProductIdAndStatusAndRating(String productId, ReviewStatus status, Integer rating);

    Page<ProductReview> findByProductIdAndStatusAndRating(String productId, ReviewStatus status, Integer rating, Pageable pageable);

    Long countByStatus(ReviewStatus status);
}

