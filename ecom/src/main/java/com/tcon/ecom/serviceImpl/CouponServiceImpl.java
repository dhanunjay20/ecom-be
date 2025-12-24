package com.tcon.ecom.serviceImpl;

import com.tcon.ecom.dto.request.ValidateCouponRequest;
import com.tcon.ecom.dto.response.CouponValidationResponse;
import com.tcon.ecom.model.Coupon;
import com.tcon.ecom.model.enums.CouponStatus;
import com.tcon.ecom.model.enums.CouponType;
import com.tcon.ecom.repository.CouponRepository;
import com.tcon.ecom.service.CouponService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
@Slf4j
public class CouponServiceImpl implements CouponService {

    private final CouponRepository couponRepository;

    @Override
    public CouponValidationResponse validateCoupon(String userId, ValidateCouponRequest request) {
        Coupon coupon = couponRepository.findByCodeAndStatus(request.getCode(), CouponStatus.ACTIVE)
                .orElse(null);

        if (coupon == null) {
            return new CouponValidationResponse(false, BigDecimal.ZERO, "Invalid coupon code");
        }

        // Check expiry
        if (coupon.getExpiryDate() != null && coupon.getExpiryDate().isBefore(LocalDateTime.now())) {
            return new CouponValidationResponse(false, BigDecimal.ZERO, "Coupon has expired");
        }

        // Check usage limit
        if (coupon.getUsedCount() >= coupon.getUsageLimit()) {
            return new CouponValidationResponse(false, BigDecimal.ZERO, "Coupon usage limit reached");
        }

        // Calculate cart total
        BigDecimal cartTotal = request.getItems().stream()
                .map(item -> item.getPrice().multiply(BigDecimal.valueOf(item.getQuantity())))
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        // Check minimum purchase
        if (coupon.getMinPurchase() != null && cartTotal.compareTo(coupon.getMinPurchase()) < 0) {
            return new CouponValidationResponse(false, BigDecimal.ZERO,
                    "Minimum purchase amount not met. Required: " + coupon.getMinPurchase());
        }

        // Calculate discount
        BigDecimal discount;
        if (coupon.getType() == CouponType.PERCENTAGE) {
            discount = cartTotal.multiply(coupon.getValue()).divide(BigDecimal.valueOf(100));
            if (coupon.getMaxDiscount() != null && discount.compareTo(coupon.getMaxDiscount()) > 0) {
                discount = coupon.getMaxDiscount();
            }
        } else {
            discount = coupon.getValue();
        }

        log.info("Coupon validated: {} for user: {}, discount: {}", request.getCode(), userId, discount);
        return new CouponValidationResponse(true, discount, "Coupon applied successfully");
    }
}

