package com.tcon.ecom.service;

import com.tcon.ecom.dto.request.CreateOrderRequest;
import com.tcon.ecom.dto.request.SubmitReviewRequest;
import com.tcon.ecom.dto.response.OrderResponse;
import com.tcon.ecom.dto.response.OrderTrackingResponse;
import com.tcon.ecom.dto.response.ProductReviewResponse;
import org.springframework.data.domain.Page;

public interface OrderService {
    OrderResponse createOrder(String email, CreateOrderRequest request);
    Page<OrderResponse> getUserOrders(String email, String status, int page, int limit);
    OrderResponse getOrderDetails(String email, String orderNumber);
    OrderTrackingResponse trackOrder(String email, String orderNumber);
    void cancelOrder(String email, String orderNumber, String reason);
    ProductReviewResponse submitReview(String email, String orderId, String productId, SubmitReviewRequest request);
}

