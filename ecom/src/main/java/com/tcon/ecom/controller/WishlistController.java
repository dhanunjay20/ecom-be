package com.tcon.ecom.controller;

import com.tcon.ecom.dto.response.ApiResponse;
import com.tcon.ecom.dto.response.WishlistResponse;
import com.tcon.ecom.service.WishlistService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/users/me/wishlist")
@Tag(name = "Wishlist", description = "User wishlist management APIs")
@SecurityRequirement(name = "Bearer Authentication")
@RequiredArgsConstructor
public class WishlistController {

    private final WishlistService wishlistService;

    @GetMapping
    @Operation(summary = "Get user wishlist")
    public ResponseEntity<ApiResponse<Page<WishlistResponse>>> getWishlist(
            @AuthenticationPrincipal UserDetails userDetails,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "20") int limit) {
        Page<WishlistResponse> wishlist = wishlistService.getWishlist(userDetails.getUsername(), page - 1, limit);
        return ResponseEntity.ok(ApiResponse.success(wishlist));
    }

    @PostMapping
    @Operation(summary = "Add product to wishlist")
    public ResponseEntity<ApiResponse<WishlistResponse>> addToWishlist(
            @AuthenticationPrincipal UserDetails userDetails,
            @RequestBody Map<String, String> request) {
        String productId = request.get("productId");
        WishlistResponse wishlistItem = wishlistService.addToWishlist(userDetails.getUsername(), productId);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("Product added to wishlist", wishlistItem));
    }

    @DeleteMapping("/{productId}")
    @Operation(summary = "Remove product from wishlist")
    public ResponseEntity<ApiResponse<String>> removeFromWishlist(
            @AuthenticationPrincipal UserDetails userDetails,
            @PathVariable String productId) {
        wishlistService.removeFromWishlist(userDetails.getUsername(), productId);
        return ResponseEntity.ok(ApiResponse.success("Product removed from wishlist", null));
    }

    @GetMapping("/check/{productId}")
    @Operation(summary = "Check if product is in wishlist")
    public ResponseEntity<ApiResponse<Map<String, Boolean>>> checkWishlist(
            @AuthenticationPrincipal UserDetails userDetails,
            @PathVariable String productId) {
        boolean inWishlist = wishlistService.isInWishlist(userDetails.getUsername(), productId);
        return ResponseEntity.ok(ApiResponse.success(Map.of("inWishlist", inWishlist)));
    }
}

