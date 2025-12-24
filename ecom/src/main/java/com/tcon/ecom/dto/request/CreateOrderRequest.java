package com.tcon.ecom.dto.request;

import com.tcon.ecom.model.enums.ShippingMethod;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CreateOrderRequest {

    @NotBlank(message = "Shipping address ID is required")
    private String shippingAddressId;

    private String billingAddressId; // Optional, defaults to shipping

    @NotBlank(message = "Payment method ID is required")
    private String paymentMethodId;

    @NotNull(message = "Shipping method is required")
    private ShippingMethod shippingMethod;

    @NotEmpty(message = "Order items cannot be empty")
    private List<OrderItemRequest> items;

    private String couponCode;
    private String customerNotes;

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class OrderItemRequest {
        @NotBlank(message = "Product ID is required")
        private String productId;

        @NotNull(message = "Quantity is required")
        private Integer quantity;

        private String selectedColor;
        private String selectedSize;
    }
}

