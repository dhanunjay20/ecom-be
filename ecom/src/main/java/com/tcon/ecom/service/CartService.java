package com.tcon.ecom.service;

import com.tcon.ecom.dto.request.AddToCartRequest;
import com.tcon.ecom.dto.request.UpdateCartItemRequest;
import com.tcon.ecom.dto.response.CartResponse;

public interface CartService {
    CartResponse getCart(String userId, String sessionId);
    CartResponse addToCart(String userId, String sessionId, AddToCartRequest request);
    CartResponse updateCartItem(String userId, String sessionId, String itemId, UpdateCartItemRequest request);
    void removeFromCart(String userId, String sessionId, String itemId);
    void clearCart(String userId, String sessionId);
    CartResponse mergeCart(String userId, String guestSessionId);
}

