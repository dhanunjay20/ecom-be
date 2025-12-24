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
public class VendorResponse {
    private String id;
    private String email;
    private String status;
    private String storeName;
    private String contactPerson;
}

