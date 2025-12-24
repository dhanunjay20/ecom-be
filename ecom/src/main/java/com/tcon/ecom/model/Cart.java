package com.tcon.ecom.model;

import com.tcon.ecom.model.embedded.CartItem;
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
import java.util.ArrayList;
import java.util.List;

@Document(collection = "carts")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Cart {

    @Id
    private String id;

    @Indexed(unique = true, sparse = true)
    private String userId; // For logged-in users

    @Indexed
    private String sessionId; // For guest users

    private List<CartItem> items = new ArrayList<>();

    // Metadata
    @Indexed
    private LocalDateTime lastActivityAt;

    @Indexed
    private LocalDateTime expiresAt; // Auto-delete old carts

    // Totals (Denormalized for performance)
    private BigDecimal subtotal = BigDecimal.ZERO;
    private Integer itemCount = 0;

    // Timestamps
    @CreatedDate
    private LocalDateTime createdAt;

    @LastModifiedDate
    private LocalDateTime updatedAt;
}

