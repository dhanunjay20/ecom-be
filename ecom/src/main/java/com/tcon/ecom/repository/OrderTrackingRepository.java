package com.tcon.ecom.repository;

import com.tcon.ecom.model.OrderTracking;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface OrderTrackingRepository extends MongoRepository<OrderTracking, String> {

    Optional<OrderTracking> findByOrderId(String orderId);

    Boolean existsByOrderId(String orderId);
}

