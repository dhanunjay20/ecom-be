package com.tcon.ecom.controller;

import com.tcon.ecom.dto.request.AddToCartRequest;
import com.tcon.ecom.dto.request.MergeCartRequest;
import com.tcon.ecom.dto.request.UpdateCartItemRequest;
import com.tcon.ecom.dto.response.ApiResponse;
import com.tcon.ecom.dto.response.CartResponse;
import com.tcon.ecom.service.CartService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/cart")
@Tag(name = "Shopping Cart", description = "Shopping cart management APIs")
@RequiredArgsConstructor
public class CartController {

    private final CartService cartService;

    @GetMapping
    @Operation(summary = "Get user cart")
    public ResponseEntity<ApiResponse<CartResponse>> getCart(
            @AuthenticationPrincipal UserDetails userDetails,
            @RequestHeader(value = "X-Session-Id", required = false) String sessionId) {
        String userId = userDetails != null ? userDetails.getUsername() : null;
        CartResponse cart = cartService.getCart(userId, sessionId);
        return ResponseEntity.ok(ApiResponse.success(cart));
    }

    @PostMapping("/items")
    @Operation(summary = "Add item to cart")
    public ResponseEntity<ApiResponse<CartResponse>> addToCart(
            @AuthenticationPrincipal UserDetails userDetails,
            @RequestHeader(value = "X-Session-Id", required = false) String sessionId,
            @Valid @RequestBody AddToCartRequest request) {
        String userId = userDetails != null ? userDetails.getUsername() : null;
        CartResponse cart = cartService.addToCart(userId, sessionId, request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("Item added to cart", cart));
    }

    @PatchMapping("/items/{itemId}")
    @Operation(summary = "Update cart item quantity")
    public ResponseEntity<ApiResponse<CartResponse>> updateCartItem(
            @AuthenticationPrincipal UserDetails userDetails,
            @RequestHeader(value = "X-Session-Id", required = false) String sessionId,
            @PathVariable String itemId,
            @Valid @RequestBody UpdateCartItemRequest request) {
        String userId = userDetails != null ? userDetails.getUsername() : null;
        CartResponse cart = cartService.updateCartItem(userId, sessionId, itemId, request);
        return ResponseEntity.ok(ApiResponse.success("Cart updated", cart));
    }

    @DeleteMapping("/items/{itemId}")
    @Operation(summary = "Remove item from cart")
    public ResponseEntity<ApiResponse<String>> removeFromCart(
            @AuthenticationPrincipal UserDetails userDetails,
            @RequestHeader(value = "X-Session-Id", required = false) String sessionId,
            @PathVariable String itemId) {
        String userId = userDetails != null ? userDetails.getUsername() : null;
        cartService.removeFromCart(userId, sessionId, itemId);
        return ResponseEntity.ok(ApiResponse.success("Item removed from cart", null));
    }

    @DeleteMapping
    @Operation(summary = "Clear cart")
    public ResponseEntity<ApiResponse<String>> clearCart(
            @AuthenticationPrincipal UserDetails userDetails,
            @RequestHeader(value = "X-Session-Id", required = false) String sessionId) {
        String userId = userDetails != null ? userDetails.getUsername() : null;
        cartService.clearCart(userId, sessionId);
        return ResponseEntity.ok(ApiResponse.success("Cart cleared", null));
    }

    @PostMapping("/merge")
    @Operation(summary = "Merge guest cart with user cart on login")
    public ResponseEntity<ApiResponse<CartResponse>> mergeCart(
            @AuthenticationPrincipal UserDetails userDetails,
            @Valid @RequestBody MergeCartRequest request) {
        CartResponse cart = cartService.mergeCart(userDetails.getUsername(), request.getGuestSessionId());
        return ResponseEntity.ok(ApiResponse.success("Carts merged successfully", cart));
    }
}

