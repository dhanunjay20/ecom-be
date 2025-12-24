package com.tcon.ecom.serviceImpl;

import com.tcon.ecom.dto.response.WishlistResponse;
import com.tcon.ecom.exception.BadRequestException;
import com.tcon.ecom.exception.ResourceNotFoundException;
import com.tcon.ecom.model.Product;
import com.tcon.ecom.model.User;
import com.tcon.ecom.model.Wishlist;
import com.tcon.ecom.repository.ProductRepository;
import com.tcon.ecom.repository.UserRepository;
import com.tcon.ecom.repository.WishlistRepository;
import com.tcon.ecom.service.WishlistService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
@Slf4j
public class WishlistServiceImpl implements WishlistService {

    private final WishlistRepository wishlistRepository;
    private final ProductRepository productRepository;
    private final UserRepository userRepository;

    @Override
    public Page<WishlistResponse> getWishlist(String email, int page, int limit) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        Pageable pageable = PageRequest.of(page, limit, Sort.by(Sort.Direction.DESC, "addedAt"));
        Page<Wishlist> wishlistPage = wishlistRepository.findByUserId(user.getId(), pageable);

        return wishlistPage.map(this::mapToWishlistResponse);
    }

    @Override
    @Transactional
    public WishlistResponse addToWishlist(String email, String productId) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new ResourceNotFoundException("Product not found"));

        // Check if already in wishlist
        if (wishlistRepository.existsByUserIdAndProductId(user.getId(), productId)) {
            throw new BadRequestException("Product is already in wishlist");
        }

        Wishlist wishlist = new Wishlist();
        wishlist.setUserId(user.getId());
        wishlist.setProductId(productId);
        wishlist.setPriceAtAdd(product.getPrice());
        wishlist.setAddedAt(LocalDateTime.now());

        Wishlist saved = wishlistRepository.save(wishlist);
        log.info("Product added to wishlist for user: {}", email);

        return mapToWishlistResponse(saved);
    }

    @Override
    @Transactional
    public void removeFromWishlist(String email, String productId) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        wishlistRepository.findByUserIdAndProductId(user.getId(), productId)
                .orElseThrow(() -> new ResourceNotFoundException("Wishlist item not found"));

        wishlistRepository.deleteByUserIdAndProductId(user.getId(), productId);
        log.info("Product removed from wishlist for user: {}", email);
    }

    @Override
    public boolean isInWishlist(String email, String productId) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        return wishlistRepository.existsByUserIdAndProductId(user.getId(), productId);
    }

    private WishlistResponse mapToWishlistResponse(Wishlist wishlist) {
        Product product = productRepository.findById(wishlist.getProductId()).orElse(null);

        WishlistResponse response = new WishlistResponse();
        response.setId(wishlist.getId());
        response.setPriceAtAdd(wishlist.getPriceAtAdd());
        response.setAddedAt(wishlist.getAddedAt());

        if (product != null) {
            WishlistResponse.ProductInfo productInfo = new WishlistResponse.ProductInfo();
            productInfo.setId(product.getId());
            productInfo.setName(product.getName());
            productInfo.setSlug(product.getSlug());
            productInfo.setPrice(product.getPrice());
            productInfo.setComparePrice(product.getComparePrice());
            productInfo.setPrimaryImage(product.getImages().isEmpty() ? null :
                    product.getImages().get(0).getUrl());
            productInfo.setCategory(product.getCategory().name());
            productInfo.setSubcategory(product.getSubcategory());
            productInfo.setInStock(product.getStock() > 0);
            productInfo.setAverageRating(product.getAverageRating());

            // Check if price dropped
            boolean priceDropped = wishlist.getPriceAtAdd() != null &&
                    product.getPrice().compareTo(wishlist.getPriceAtAdd()) < 0;
            response.setPriceDropped(priceDropped);

            response.setProduct(productInfo);
        }

        return response;
    }
}

