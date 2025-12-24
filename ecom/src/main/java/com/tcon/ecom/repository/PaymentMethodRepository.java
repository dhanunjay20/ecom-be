package com.tcon.ecom.repository;

import com.tcon.ecom.model.PaymentMethod;
import com.tcon.ecom.model.enums.PaymentMethodStatus;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface PaymentMethodRepository extends MongoRepository<PaymentMethod, String> {

    List<PaymentMethod> findByUserId(String userId);

    List<PaymentMethod> findByUserIdAndStatus(String userId, PaymentMethodStatus status);

    Optional<PaymentMethod> findByUserIdAndIsDefaultTrue(String userId);

    Optional<PaymentMethod> findByStripePaymentMethodId(String stripePaymentMethodId);

    Long countByUserId(String userId);
}

