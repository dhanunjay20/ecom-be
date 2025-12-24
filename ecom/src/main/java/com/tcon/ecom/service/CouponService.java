package com.tcon.ecom.service;

import com.tcon.ecom.dto.request.ValidateCouponRequest;
import com.tcon.ecom.dto.response.CouponValidationResponse;

public interface CouponService {
    CouponValidationResponse validateCoupon(String userId, ValidateCouponRequest request);
}

