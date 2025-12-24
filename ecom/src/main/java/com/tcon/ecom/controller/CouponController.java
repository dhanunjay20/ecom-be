package com.tcon.ecom.controller;

import com.tcon.ecom.dto.request.ValidateCouponRequest;
import com.tcon.ecom.dto.response.ApiResponse;
import com.tcon.ecom.dto.response.CouponValidationResponse;
import com.tcon.ecom.service.CouponService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/coupons")
@Tag(name = "Coupons", description = "Coupon validation APIs")
@RequiredArgsConstructor
public class CouponController {

    private final CouponService couponService;

    @PostMapping("/validate")
    @Operation(summary = "Validate coupon code")
    public ResponseEntity<ApiResponse<CouponValidationResponse>> validateCoupon(
            @AuthenticationPrincipal UserDetails userDetails,
            @Valid @RequestBody ValidateCouponRequest request) {
        String userId = userDetails != null ? userDetails.getUsername() : null;
        CouponValidationResponse validation = couponService.validateCoupon(userId, request);
        return ResponseEntity.ok(ApiResponse.success(validation));
    }
}

