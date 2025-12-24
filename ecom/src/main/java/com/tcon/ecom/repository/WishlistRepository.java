package com.tcon.ecom.repository;

import com.tcon.ecom.model.Wishlist;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface WishlistRepository extends MongoRepository<Wishlist, String> {

    List<Wishlist> findByUserId(String userId);

    Page<Wishlist> findByUserId(String userId, Pageable pageable);

    Optional<Wishlist> findByUserIdAndProductId(String userId, String productId);

    Boolean existsByUserIdAndProductId(String userId, String productId);

    Long countByUserId(String userId);

    void deleteByUserIdAndProductId(String userId, String productId);
}

