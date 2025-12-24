package com.tcon.ecom.service;

import com.tcon.ecom.dto.request.*;
import com.tcon.ecom.dto.response.*;
import org.springframework.data.domain.Page;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDate;
import java.util.List;

public interface VendorService {
    VendorResponse registerVendor(VendorRegisterRequest request);
    VendorDashboardStats getDashboardStats(String email, String period);
    VendorAnalytics getAnalytics(String email, LocalDate startDate, LocalDate endDate, String groupBy);
    Page<ProductListResponse> getVendorProducts(String email, String status, String category, String search, int page, int limit);
    ProductDetailResponse createProduct(String email, CreateProductRequest request, List<MultipartFile> images);
    ProductDetailResponse updateProduct(String email, String productId, UpdateProductRequest request);
    void deleteProduct(String email, String productId);
    Page<OrderResponse> getVendorOrders(String email, String status, LocalDate startDate, LocalDate endDate, int page, int limit);
    void updateOrderStatus(String email, String orderNumber, UpdateOrderStatusRequest request);
    CouponResponse createCoupon(String email, CreateCouponRequest request);
    List<CouponResponse> getVendorCoupons(String email, String status);
}

