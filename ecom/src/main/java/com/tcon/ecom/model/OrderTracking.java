package com.tcon.ecom.model;

import com.tcon.ecom.model.embedded.TrackingEvent;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Document(collection = "order_tracking")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class OrderTracking {

    @Id
    private String id;

    @NotBlank
    @Indexed(unique = true)
    private String orderId;

    // Tracking Events
    private List<TrackingEvent> events = new ArrayList<>();

    // Timestamps
    @CreatedDate
    private LocalDateTime createdAt;

    @LastModifiedDate
    private LocalDateTime updatedAt;
}

