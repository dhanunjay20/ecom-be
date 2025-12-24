package com.tcon.ecom.model;

import com.tcon.ecom.model.enums.PaymentMethodStatus;
import com.tcon.ecom.model.enums.PaymentMethodType;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.mongodb.core.index.CompoundIndex;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDateTime;

@Document(collection = "payment_methods")
@Data
@NoArgsConstructor
@AllArgsConstructor
@CompoundIndex(name = "userId_isDefault", def = "{'userId': 1, 'isDefault': 1}")
public class PaymentMethod {

    @Id
    private String id;

    @NotBlank
    @Indexed
    private String userId;

    private PaymentMethodType type;
    private Boolean isDefault = false;

    // Card Details (PCI Compliant - Never store CVV)
    @NotBlank
    private String last4;

    @NotBlank
    private String cardholderName;

    private Integer expiryMonth;
    private Integer expiryYear;

    // Payment Gateway Reference
    @Indexed
    private String stripePaymentMethodId;

    private String fingerprint; // Card fingerprint for duplicate detection

    // Billing Address
    private String billingAddressId; // Reference to addresses

    // Status
    private PaymentMethodStatus status = PaymentMethodStatus.ACTIVE;

    // Timestamps
    @CreatedDate
    private LocalDateTime createdAt;

    @LastModifiedDate
    private LocalDateTime updatedAt;

    private LocalDateTime deletedAt;
}

