package com.tcon.ecom.model.embedded;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class TrackingEvent {
    private String status;
    private String location;
    private LocalDateTime timestamp;
    private String description;
    private Boolean isCustomerVisible = true;
}

