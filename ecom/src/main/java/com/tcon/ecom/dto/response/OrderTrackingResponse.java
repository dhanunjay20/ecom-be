package com.tcon.ecom.dto.response;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class OrderTrackingResponse {
    private String orderNumber;
    private String status;
    private String trackingNumber;
    private String carrier;
    private LocalDateTime estimatedDelivery;
    private LocalDateTime actualDelivery;
    private List<TrackingEvent> events;

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class TrackingEvent {
        private String status;
        private String location;
        private LocalDateTime timestamp;
        private String description;
        private Boolean isCustomerVisible;
    }
}

