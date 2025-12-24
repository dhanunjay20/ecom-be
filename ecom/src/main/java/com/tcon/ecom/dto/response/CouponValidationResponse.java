package com.tcon.ecom.dto.response;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CouponValidationResponse {
    private Boolean valid;
    private BigDecimal discount;
    private String message;
}

