package com.tcon.ecom.model;

import com.tcon.ecom.model.embedded.VendorResponse;
import com.tcon.ecom.model.enums.ReviewStatus;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.mongodb.core.index.CompoundIndex;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDateTime;
import java.util.List;

@Document(collection = "product_reviews")
@Data
@NoArgsConstructor
@AllArgsConstructor
@CompoundIndex(name = "productId_userId", def = "{'productId': 1, 'userId': 1}", unique = true)
public class ProductReview {

    @Id
    private String id;

    @NotBlank
    @Indexed
    private String productId;

    @NotBlank
    @Indexed
    private String userId;

    @NotBlank
    private String orderId; // Verify purchase

    // Review Content
    @Min(1)
    @Max(5)
    @Indexed
    private Integer rating;

    @NotBlank
    private String title;

    @NotBlank
    private String comment;

    // Media
    private List<String> images; // URLs

    // Verification
    private Boolean isVerifiedPurchase = false;

    // Moderation
    @Indexed
    private ReviewStatus status = ReviewStatus.PENDING;

    // Helpfulness
    private Integer helpfulCount = 0;

    // Vendor Response
    private VendorResponse vendorResponse;

    // Timestamps
    @CreatedDate
    private LocalDateTime createdAt;

    @LastModifiedDate
    private LocalDateTime updatedAt;
}

