package com.tcon.ecom.repository;

import com.tcon.ecom.model.Vendor;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface VendorRepository extends MongoRepository<Vendor, String> {
    Optional<Vendor> findByUserId(String userId);
    Optional<Vendor> findByEmail(String email);
    boolean existsByEmail(String email);
    boolean existsByUserId(String userId);
    boolean existsByTaxId(String taxId);
}

