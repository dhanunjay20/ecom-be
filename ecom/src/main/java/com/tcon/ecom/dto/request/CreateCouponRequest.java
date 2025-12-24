package com.tcon.ecom.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CreateCouponRequest {

    @NotBlank(message = "Coupon code is required")
    private String code;

    @NotBlank(message = "Type is required")
    private String type; // percentage or fixed

    @NotNull(message = "Value is required")
    private BigDecimal value;

    private BigDecimal minPurchase;
    private BigDecimal maxDiscount;

    @NotNull(message = "Usage limit is required")
    private Integer usageLimit;

    @NotNull(message = "Expiry date is required")
    private LocalDateTime expiryDate;

    private List<String> applicableProducts;
    private List<String> applicableCategories;
}

