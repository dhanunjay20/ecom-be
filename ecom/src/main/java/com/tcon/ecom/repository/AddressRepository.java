package com.tcon.ecom.repository;

import com.tcon.ecom.model.Address;
import com.tcon.ecom.model.enums.AddressType;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface AddressRepository extends MongoRepository<Address, String> {

    List<Address> findByUserId(String userId);

    List<Address> findByUserIdAndType(String userId, AddressType type);

    Optional<Address> findByUserIdAndIsDefaultTrue(String userId);

    Long countByUserId(String userId);
}

