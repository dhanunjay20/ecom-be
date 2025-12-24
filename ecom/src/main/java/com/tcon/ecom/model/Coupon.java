package com.tcon.ecom.model;

import com.tcon.ecom.model.enums.CouponStatus;
import com.tcon.ecom.model.enums.CouponType;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Document(collection = "coupons")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Coupon {

    @Id
    private String id;

    @NotBlank
    @Indexed(unique = true)
    private String code;

    @NotBlank
    @Indexed
    private String vendorId;

    private CouponType type; // percentage or fixed

    private BigDecimal value;

    private BigDecimal minPurchase;

    private BigDecimal maxDiscount; // For percentage type

    private Integer usageLimit;

    private Integer usedCount = 0;

    private LocalDateTime expiryDate;

    private List<String> applicableProducts;

    private List<String> applicableCategories;

    @Indexed
    private CouponStatus status = CouponStatus.ACTIVE;

    @CreatedDate
    private LocalDateTime createdAt;

    @LastModifiedDate
    private LocalDateTime updatedAt;
}

