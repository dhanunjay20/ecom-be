package com.tcon.ecom.controller;

import com.tcon.ecom.dto.request.CancelOrderRequest;
import com.tcon.ecom.dto.request.CreateOrderRequest;
import com.tcon.ecom.dto.request.SubmitReviewRequest;
import com.tcon.ecom.dto.response.ApiResponse;
import com.tcon.ecom.dto.response.OrderResponse;
import com.tcon.ecom.dto.response.OrderTrackingResponse;
import com.tcon.ecom.dto.response.ProductReviewResponse;
import com.tcon.ecom.service.OrderService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api")
@Tag(name = "Orders", description = "Order management APIs")
@SecurityRequirement(name = "Bearer Authentication")
@RequiredArgsConstructor
public class OrderController {

    private final OrderService orderService;

    @PostMapping("/orders")
    @Operation(summary = "Create order (checkout)")
    public ResponseEntity<ApiResponse<OrderResponse>> createOrder(
            @AuthenticationPrincipal UserDetails userDetails,
            @Valid @RequestBody CreateOrderRequest request) {
        OrderResponse order = orderService.createOrder(userDetails.getUsername(), request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("Order placed successfully", order));
    }

    @GetMapping("/users/me/orders")
    @Operation(summary = "Get user orders")
    public ResponseEntity<ApiResponse<Page<OrderResponse>>> getUserOrders(
            @AuthenticationPrincipal UserDetails userDetails,
            @RequestParam(required = false) String status,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int limit) {
        Page<OrderResponse> orders = orderService.getUserOrders(userDetails.getUsername(), status, page - 1, limit);
        return ResponseEntity.ok(ApiResponse.success(orders));
    }

    @GetMapping("/users/me/orders/{orderNumber}")
    @Operation(summary = "Get order details")
    public ResponseEntity<ApiResponse<OrderResponse>> getOrderDetails(
            @AuthenticationPrincipal UserDetails userDetails,
            @PathVariable String orderNumber) {
        OrderResponse order = orderService.getOrderDetails(userDetails.getUsername(), orderNumber);
        return ResponseEntity.ok(ApiResponse.success(order));
    }

    @GetMapping("/users/me/orders/{orderNumber}/tracking")
    @Operation(summary = "Track order")
    public ResponseEntity<ApiResponse<OrderTrackingResponse>> trackOrder(
            @AuthenticationPrincipal UserDetails userDetails,
            @PathVariable String orderNumber) {
        OrderTrackingResponse tracking = orderService.trackOrder(userDetails.getUsername(), orderNumber);
        return ResponseEntity.ok(ApiResponse.success(tracking));
    }

    @PostMapping("/users/me/orders/{orderNumber}/cancel")
    @Operation(summary = "Cancel order")
    public ResponseEntity<ApiResponse<String>> cancelOrder(
            @AuthenticationPrincipal UserDetails userDetails,
            @PathVariable String orderNumber,
            @Valid @RequestBody CancelOrderRequest request) {
        orderService.cancelOrder(userDetails.getUsername(), orderNumber, request.getReason());
        return ResponseEntity.ok(ApiResponse.success("Order cancelled successfully", null));
    }

    @PostMapping("/orders/{orderId}/products/{productId}/review")
    @Operation(summary = "Submit product review")
    public ResponseEntity<ApiResponse<ProductReviewResponse>> submitReview(
            @AuthenticationPrincipal UserDetails userDetails,
            @PathVariable String orderId,
            @PathVariable String productId,
            @Valid @RequestBody SubmitReviewRequest request) {
        ProductReviewResponse review = orderService.submitReview(
                userDetails.getUsername(), orderId, productId, request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("Review submitted successfully", review));
    }
}

