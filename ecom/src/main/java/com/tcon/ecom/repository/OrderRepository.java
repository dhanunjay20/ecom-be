package com.tcon.ecom.repository;

import com.tcon.ecom.model.Order;
import com.tcon.ecom.model.enums.OrderStatus;
import com.tcon.ecom.model.enums.PaymentStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface OrderRepository extends MongoRepository<Order, String> {

    Optional<Order> findByOrderNumber(String orderNumber);

    Page<Order> findByUserId(String userId, Pageable pageable);

    Page<Order> findByUserIdAndStatus(String userId, OrderStatus status, Pageable pageable);

    List<Order> findByUserIdOrderByCreatedAtDesc(String userId);

    Page<Order> findByStatus(OrderStatus status, Pageable pageable);

    Page<Order> findByPaymentStatus(PaymentStatus paymentStatus, Pageable pageable);

    List<Order> findByCreatedAtBetween(LocalDateTime start, LocalDateTime end);

    // For vendor - find orders containing their products
    // This would need a custom query or aggregation

    Long countByUserId(String userId);

    Long countByStatus(OrderStatus status);

    Long countByPaymentStatus(PaymentStatus paymentStatus);

    Boolean existsByOrderNumber(String orderNumber);
}

