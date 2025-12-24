package com.tcon.ecom.dto.response;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ProductReviewResponse {
    private String id;
    private ReviewUser user;
    private Integer rating;
    private String title;
    private String comment;
    private List<String> images;
    private Boolean isVerifiedPurchase;
    private Integer helpfulCount;
    private VendorResponse vendorResponse;
    private LocalDateTime createdAt;

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ReviewUser {
        private String firstName;
        private String lastNameInitial;
        private String avatar;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class VendorResponse {
        private String comment;
        private LocalDateTime respondedAt;
    }
}

