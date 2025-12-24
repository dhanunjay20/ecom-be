package com.tcon.ecom.service;

import com.tcon.ecom.dto.response.WishlistResponse;
import org.springframework.data.domain.Page;

public interface WishlistService {
    Page<WishlistResponse> getWishlist(String email, int page, int limit);
    WishlistResponse addToWishlist(String email, String productId);
    void removeFromWishlist(String email, String productId);
    boolean isInWishlist(String email, String productId);
}

