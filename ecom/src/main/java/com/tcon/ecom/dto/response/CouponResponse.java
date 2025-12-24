package com.tcon.ecom.dto.response;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CouponResponse {
    private String id;
    private String code;
    private String type;
    private BigDecimal value;
    private Integer usedCount;
    private Integer usageLimit;
    private LocalDateTime expiryDate;
    private String status;
}

