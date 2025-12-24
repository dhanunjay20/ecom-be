package com.tcon.ecom.model;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.CompoundIndex;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Document(collection = "wishlists")
@Data
@NoArgsConstructor
@AllArgsConstructor
@CompoundIndex(name = "userId_productId", def = "{'userId': 1, 'productId': 1}", unique = true)
public class Wishlist {

    @Id
    private String id;

    @NotBlank
    @Indexed
    private String userId;

    @NotBlank
    @Indexed
    private String productId;

    // Metadata
    private String notes; // User notes
    private BigDecimal priceAtAdd; // Track price changes

    // Timestamps
    private LocalDateTime addedAt;
}

