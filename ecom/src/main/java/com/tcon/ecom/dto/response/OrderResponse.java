package com.tcon.ecom.dto.response;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class OrderResponse {
    private String id;
    private String orderNumber;
    private List<OrderItemResponse> items;
    private BigDecimal subtotal;
    private BigDecimal shippingCost;
    private BigDecimal tax;
    private BigDecimal discount;
    private BigDecimal total;
    private String status;
    private String paymentStatus;
    private String trackingNumber;
    private String carrier;
    private LocalDateTime estimatedDelivery;
    private LocalDateTime actualDelivery;
    private ShippingAddressResponse shippingAddress;
    private LocalDateTime createdAt;

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class OrderItemResponse {
        private String productId;
        private String name;
        private String sku;
        private Integer quantity;
        private BigDecimal price;
        private String selectedColor;
        private String selectedSize;
        private String image;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ShippingAddressResponse {
        private String fullName;
        private String street;
        private String apartment;
        private String city;
        private String state;
        private String zipCode;
        private String country;
        private String phone;
    }
}

