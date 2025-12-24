package com.tcon.ecom.dto.request;

import com.tcon.ecom.model.enums.OrderStatus;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class UpdateOrderStatusRequest {

    @NotNull(message = "Status is required")
    private OrderStatus status; // processing, confirmed, shipped

    private String trackingNumber; // Required if status is shipped
    private String carrier; // Required if status is shipped
    private String internalNotes;
}

