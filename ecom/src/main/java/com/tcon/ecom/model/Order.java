package com.tcon.ecom.model;

import com.tcon.ecom.model.embedded.BillingAddress;
import com.tcon.ecom.model.embedded.OrderItem;
import com.tcon.ecom.model.embedded.ShippingAddress;
import com.tcon.ecom.model.embedded.StatusHistory;
import com.tcon.ecom.model.enums.OrderStatus;
import com.tcon.ecom.model.enums.PaymentMethodEnum;
import com.tcon.ecom.model.enums.PaymentStatus;
import com.tcon.ecom.model.enums.ShippingMethod;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.mongodb.core.index.CompoundIndex;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Document(collection = "orders")
@Data
@NoArgsConstructor
@AllArgsConstructor
@CompoundIndex(name = "userId_createdAt", def = "{'userId': 1, 'createdAt': -1}")
public class Order {

    @Id
    private String id;

    @NotBlank
    @Indexed(unique = true)
    private String orderNumber; // e.g., #ORD-1234

    // User Information
    @NotBlank
    @Indexed
    private String userId;

    @NotBlank
    private String customerEmail;

    // Order Items
    private List<OrderItem> items = new ArrayList<>();

    // Pricing
    private BigDecimal subtotal;
    private BigDecimal shippingCost;
    private BigDecimal tax;
    private BigDecimal discount = BigDecimal.ZERO;
    private BigDecimal total;

    // Coupon
    private String couponCode;
    private BigDecimal couponDiscount = BigDecimal.ZERO;

    // Shipping Address (Snapshot)
    private ShippingAddress shippingAddress;

    // Billing Address (Snapshot)
    private BillingAddress billingAddress;

    // Shipping
    private ShippingMethod shippingMethod;
    private String trackingNumber;
    private String carrier;
    private LocalDateTime estimatedDelivery;
    private LocalDateTime actualDelivery;

    // Payment
    private PaymentMethodEnum paymentMethod;

    @Indexed
    private PaymentStatus paymentStatus = PaymentStatus.PENDING;

    private String stripePaymentIntentId;
    private String last4; // Last 4 digits of card

    // Order Status & Workflow
    @Indexed
    private OrderStatus status = OrderStatus.PENDING;

    // Status History
    private List<StatusHistory> statusHistory = new ArrayList<>();

    // Customer Notes
    private String customerNotes;

    // Internal Notes (Vendor only)
    private String internalNotes;

    // Cancellation
    private String cancellationReason;
    private LocalDateTime cancelledAt;
    private String cancelledBy; // userId

    // Refund
    private BigDecimal refundAmount;
    private String refundReason;
    private LocalDateTime refundedAt;

    // Timestamps
    @CreatedDate
    @Indexed
    private LocalDateTime createdAt; // Order placed

    @LastModifiedDate
    private LocalDateTime updatedAt;

    private LocalDateTime paidAt;
    private LocalDateTime shippedAt;
    private LocalDateTime deliveredAt;
}

