package com.tcon.ecom.controller;

import com.tcon.ecom.dto.request.*;
import com.tcon.ecom.dto.response.*;
import com.tcon.ecom.service.VendorService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/vendors")
@Tag(name = "Vendor Management", description = "Vendor dashboard and product management APIs")
@RequiredArgsConstructor
public class VendorController {

    private final VendorService vendorService;

    @PostMapping("/register")
    @Operation(summary = "Register as a vendor")
    public ResponseEntity<ApiResponse<VendorResponse>> registerVendor(
            @Valid @RequestBody VendorRegisterRequest request) {
        VendorResponse vendor = vendorService.registerVendor(request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("Vendor registration submitted for review", vendor));
    }

    @GetMapping("/dashboard/stats")
    @SecurityRequirement(name = "Bearer Authentication")
    @Operation(summary = "Get vendor dashboard statistics")
    public ResponseEntity<ApiResponse<VendorDashboardStats>> getDashboardStats(
            @AuthenticationPrincipal UserDetails userDetails,
            @RequestParam(defaultValue = "month") String period) {
        VendorDashboardStats stats = vendorService.getDashboardStats(userDetails.getUsername(), period);
        return ResponseEntity.ok(ApiResponse.success(stats));
    }

    @GetMapping("/analytics")
    @SecurityRequirement(name = "Bearer Authentication")
    @Operation(summary = "Get vendor analytics")
    public ResponseEntity<ApiResponse<VendorAnalytics>> getAnalytics(
            @AuthenticationPrincipal UserDetails userDetails,
            @RequestParam LocalDate startDate,
            @RequestParam LocalDate endDate,
            @RequestParam(defaultValue = "day") String groupBy) {
        VendorAnalytics analytics = vendorService.getAnalytics(userDetails.getUsername(), startDate, endDate, groupBy);
        return ResponseEntity.ok(ApiResponse.success(analytics));
    }

    @GetMapping("/products")
    @SecurityRequirement(name = "Bearer Authentication")
    @Operation(summary = "Get vendor products")
    public ResponseEntity<ApiResponse<Page<ProductListResponse>>> getVendorProducts(
            @AuthenticationPrincipal UserDetails userDetails,
            @RequestParam(required = false) String status,
            @RequestParam(required = false) String category,
            @RequestParam(required = false) String search,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "20") int limit) {
        Page<ProductListResponse> products = vendorService.getVendorProducts(
                userDetails.getUsername(), status, category, search, page - 1, limit);
        return ResponseEntity.ok(ApiResponse.success(products));
    }

    @PostMapping(value = "/products", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @SecurityRequirement(name = "Bearer Authentication")
    @Operation(summary = "Create new product")
    public ResponseEntity<ApiResponse<ProductDetailResponse>> createProduct(
            @AuthenticationPrincipal UserDetails userDetails,
            @Valid @ModelAttribute CreateProductRequest request,
            @RequestParam("images") List<MultipartFile> images) {
        ProductDetailResponse product = vendorService.createProduct(userDetails.getUsername(), request, images);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("Product created successfully", product));
    }

    @PatchMapping("/products/{productId}")
    @SecurityRequirement(name = "Bearer Authentication")
    @Operation(summary = "Update product")
    public ResponseEntity<ApiResponse<ProductDetailResponse>> updateProduct(
            @AuthenticationPrincipal UserDetails userDetails,
            @PathVariable String productId,
            @Valid @RequestBody UpdateProductRequest request) {
        ProductDetailResponse product = vendorService.updateProduct(userDetails.getUsername(), productId, request);
        return ResponseEntity.ok(ApiResponse.success("Product updated successfully", product));
    }

    @DeleteMapping("/products/{productId}")
    @SecurityRequirement(name = "Bearer Authentication")
    @Operation(summary = "Delete product")
    public ResponseEntity<ApiResponse<String>> deleteProduct(
            @AuthenticationPrincipal UserDetails userDetails,
            @PathVariable String productId) {
        vendorService.deleteProduct(userDetails.getUsername(), productId);
        return ResponseEntity.ok(ApiResponse.success("Product deleted successfully", null));
    }

    @GetMapping("/orders")
    @SecurityRequirement(name = "Bearer Authentication")
    @Operation(summary = "Get vendor orders")
    public ResponseEntity<ApiResponse<Page<OrderResponse>>> getVendorOrders(
            @AuthenticationPrincipal UserDetails userDetails,
            @RequestParam(required = false) String status,
            @RequestParam(required = false) LocalDate startDate,
            @RequestParam(required = false) LocalDate endDate,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "20") int limit) {
        Page<OrderResponse> orders = vendorService.getVendorOrders(
                userDetails.getUsername(), status, startDate, endDate, page - 1, limit);
        return ResponseEntity.ok(ApiResponse.success(orders));
    }

    @PatchMapping("/orders/{orderNumber}/status")
    @SecurityRequirement(name = "Bearer Authentication")
    @Operation(summary = "Update order status")
    public ResponseEntity<ApiResponse<String>> updateOrderStatus(
            @AuthenticationPrincipal UserDetails userDetails,
            @PathVariable String orderNumber,
            @Valid @RequestBody UpdateOrderStatusRequest request) {
        vendorService.updateOrderStatus(userDetails.getUsername(), orderNumber, request);
        return ResponseEntity.ok(ApiResponse.success("Order status updated", null));
    }

    @PostMapping("/coupons")
    @SecurityRequirement(name = "Bearer Authentication")
    @Operation(summary = "Create coupon")
    public ResponseEntity<ApiResponse<CouponResponse>> createCoupon(
            @AuthenticationPrincipal UserDetails userDetails,
            @Valid @RequestBody CreateCouponRequest request) {
        CouponResponse coupon = vendorService.createCoupon(userDetails.getUsername(), request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("Coupon created successfully", coupon));
    }

    @GetMapping("/coupons")
    @SecurityRequirement(name = "Bearer Authentication")
    @Operation(summary = "Get vendor coupons")
    public ResponseEntity<ApiResponse<List<CouponResponse>>> getVendorCoupons(
            @AuthenticationPrincipal UserDetails userDetails,
            @RequestParam(defaultValue = "all") String status) {
        List<CouponResponse> coupons = vendorService.getVendorCoupons(userDetails.getUsername(), status);
        return ResponseEntity.ok(ApiResponse.success(coupons));
    }
}

