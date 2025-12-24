package com.tcon.ecom.repository;

import com.tcon.ecom.model.Coupon;
import com.tcon.ecom.model.enums.CouponStatus;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface CouponRepository extends MongoRepository<Coupon, String> {
    Optional<Coupon> findByCode(String code);
    Optional<Coupon> findByCodeAndStatus(String code, CouponStatus status);
    List<Coupon> findByVendorId(String vendorId);
    List<Coupon> findByVendorIdAndStatus(String vendorId, CouponStatus status);
}

