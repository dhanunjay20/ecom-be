package com.tcon.ecom.serviceImpl;

import com.tcon.ecom.dto.request.AddToCartRequest;
import com.tcon.ecom.dto.request.UpdateCartItemRequest;
import com.tcon.ecom.dto.response.CartResponse;
import com.tcon.ecom.exception.BadRequestException;
import com.tcon.ecom.exception.ResourceNotFoundException;
import com.tcon.ecom.model.Cart;
import com.tcon.ecom.model.Product;
import com.tcon.ecom.model.User;
import com.tcon.ecom.model.embedded.CartItem;
import com.tcon.ecom.model.enums.ProductStatus;
import com.tcon.ecom.repository.CartRepository;
import com.tcon.ecom.repository.ProductRepository;
import com.tcon.ecom.repository.UserRepository;
import com.tcon.ecom.service.CartService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class CartServiceImpl implements CartService {

    private final CartRepository cartRepository;
    private final ProductRepository productRepository;
    private final UserRepository userRepository;

    @Override
    public CartResponse getCart(String userId, String sessionId) {
        Cart cart;
        if (userId != null) {
            User user = userRepository.findByEmail(userId)
                    .orElseThrow(() -> new ResourceNotFoundException("User not found"));
            cart = cartRepository.findByUserId(user.getId())
                    .orElse(createNewCart(user.getId(), null));
        } else if (sessionId != null) {
            cart = cartRepository.findBySessionId(sessionId)
                    .orElse(createNewCart(null, sessionId));
        } else {
            // Create guest cart with new session
            String newSessionId = UUID.randomUUID().toString();
            cart = createNewCart(null, newSessionId);
        }

        return mapToCartResponse(cart);
    }

    @Override
    @Transactional
    public CartResponse addToCart(String userId, String sessionId, AddToCartRequest request) {
        // Validate product
        Product product = productRepository.findById(request.getProductId())
                .orElseThrow(() -> new ResourceNotFoundException("Product not found"));

        if (product.getStatus() != ProductStatus.ACTIVE) {
            throw new BadRequestException("Product is not available");
        }

        if (product.getStock() < request.getQuantity()) {
            throw new BadRequestException("Insufficient stock. Available: " + product.getStock());
        }

        if (request.getQuantity() < 1 || request.getQuantity() > 10) {
            throw new BadRequestException("Quantity must be between 1 and 10");
        }

        // Get or create cart
        Cart cart;
        if (userId != null) {
            User user = userRepository.findByEmail(userId)
                    .orElseThrow(() -> new ResourceNotFoundException("User not found"));
            cart = cartRepository.findByUserId(user.getId())
                    .orElse(createNewCart(user.getId(), null));
        } else {
            String effectiveSessionId = sessionId != null ? sessionId : UUID.randomUUID().toString();
            cart = cartRepository.findBySessionId(effectiveSessionId)
                    .orElse(createNewCart(null, effectiveSessionId));
        }

        // Check if item already exists
        Optional<CartItem> existingItem = cart.getItems().stream()
                .filter(item -> item.getProductId().equals(request.getProductId())
                        && equals(item.getSelectedColor(), request.getSelectedColor())
                        && equals(item.getSelectedSize(), request.getSelectedSize()))
                .findFirst();

        if (existingItem.isPresent()) {
            // Update quantity
            CartItem item = existingItem.get();
            int newQuantity = item.getQuantity() + request.getQuantity();
            if (newQuantity > 10) {
                throw new BadRequestException("Maximum quantity per item is 10");
            }
            if (newQuantity > product.getStock()) {
                throw new BadRequestException("Insufficient stock");
            }
            item.setQuantity(newQuantity);
        } else {
            // Add new item
            CartItem newItem = new CartItem();
            newItem.setProductId(request.getProductId());
            newItem.setQuantity(request.getQuantity());
            newItem.setSelectedColor(request.getSelectedColor());
            newItem.setSelectedSize(request.getSelectedSize());
            newItem.setPrice(product.getPrice());
            newItem.setAddedAt(LocalDateTime.now());
            cart.getItems().add(newItem);
        }

        // Recalculate totals
        recalculateCart(cart);
        cart.setLastActivityAt(LocalDateTime.now());
        cart.setExpiresAt(LocalDateTime.now().plusDays(30));

        Cart savedCart = cartRepository.save(cart);
        log.info("Item added to cart for user/session: {}/{}", userId, sessionId);

        return mapToCartResponse(savedCart);
    }

    @Override
    @Transactional
    public CartResponse updateCartItem(String userId, String sessionId, String itemId, UpdateCartItemRequest request) {
        Cart cart = getCartEntity(userId, sessionId);

        CartItem item = cart.getItems().stream()
                .filter(i -> i.getProductId().equals(itemId))
                .findFirst()
                .orElseThrow(() -> new ResourceNotFoundException("Cart item not found"));

        // Validate quantity
        Product product = productRepository.findById(item.getProductId())
                .orElseThrow(() -> new ResourceNotFoundException("Product not found"));

        if (request.getQuantity() > product.getStock()) {
            throw new BadRequestException("Insufficient stock. Available: " + product.getStock());
        }

        item.setQuantity(request.getQuantity());
        recalculateCart(cart);
        cart.setLastActivityAt(LocalDateTime.now());

        Cart savedCart = cartRepository.save(cart);
        log.info("Cart item updated");

        return mapToCartResponse(savedCart);
    }

    @Override
    @Transactional
    public void removeFromCart(String userId, String sessionId, String itemId) {
        Cart cart = getCartEntity(userId, sessionId);

        cart.getItems().removeIf(item -> item.getProductId().equals(itemId));
        recalculateCart(cart);
        cart.setLastActivityAt(LocalDateTime.now());

        cartRepository.save(cart);
        log.info("Item removed from cart");
    }

    @Override
    @Transactional
    public void clearCart(String userId, String sessionId) {
        Cart cart = getCartEntity(userId, sessionId);
        cart.getItems().clear();
        cart.setSubtotal(BigDecimal.ZERO);
        cart.setItemCount(0);
        cart.setLastActivityAt(LocalDateTime.now());

        cartRepository.save(cart);
        log.info("Cart cleared");
    }

    @Override
    @Transactional
    public CartResponse mergeCart(String userId, String guestSessionId) {
        User user = userRepository.findByEmail(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        // Get guest cart
        Cart guestCart = cartRepository.findBySessionId(guestSessionId)
                .orElse(null);

        if (guestCart == null || guestCart.getItems().isEmpty()) {
            // No guest cart to merge, return user cart
            return getCart(userId, null);
        }

        // Get or create user cart
        Cart userCart = cartRepository.findByUserId(user.getId())
                .orElse(createNewCart(user.getId(), null));

        // Merge items
        for (CartItem guestItem : guestCart.getItems()) {
            Optional<CartItem> existingItem = userCart.getItems().stream()
                    .filter(item -> item.getProductId().equals(guestItem.getProductId())
                            && equals(item.getSelectedColor(), guestItem.getSelectedColor())
                            && equals(item.getSelectedSize(), guestItem.getSelectedSize()))
                    .findFirst();

            if (existingItem.isPresent()) {
                // Combine quantities
                CartItem item = existingItem.get();
                int newQuantity = Math.min(item.getQuantity() + guestItem.getQuantity(), 10);
                item.setQuantity(newQuantity);
            } else {
                // Add guest item to user cart
                userCart.getItems().add(guestItem);
            }
        }

        // Recalculate and save
        recalculateCart(userCart);
        userCart.setLastActivityAt(LocalDateTime.now());
        Cart savedCart = cartRepository.save(userCart);

        // Delete guest cart
        cartRepository.delete(guestCart);
        log.info("Cart merged for user: {}", userId);

        return mapToCartResponse(savedCart);
    }

    private Cart getCartEntity(String userId, String sessionId) {
        if (userId != null) {
            User user = userRepository.findByEmail(userId)
                    .orElseThrow(() -> new ResourceNotFoundException("User not found"));
            return cartRepository.findByUserId(user.getId())
                    .orElseThrow(() -> new ResourceNotFoundException("Cart not found"));
        } else if (sessionId != null) {
            return cartRepository.findBySessionId(sessionId)
                    .orElseThrow(() -> new ResourceNotFoundException("Cart not found"));
        } else {
            throw new BadRequestException("No user ID or session ID provided");
        }
    }

    private Cart createNewCart(String userId, String sessionId) {
        Cart cart = new Cart();
        cart.setUserId(userId);
        cart.setSessionId(sessionId);
        cart.setItems(new ArrayList<>());
        cart.setSubtotal(BigDecimal.ZERO);
        cart.setItemCount(0);
        cart.setLastActivityAt(LocalDateTime.now());
        cart.setExpiresAt(LocalDateTime.now().plusDays(30));
        return cartRepository.save(cart);
    }

    private void recalculateCart(Cart cart) {
        BigDecimal subtotal = BigDecimal.ZERO;
        int itemCount = 0;

        for (CartItem item : cart.getItems()) {
            BigDecimal itemTotal = item.getPrice().multiply(BigDecimal.valueOf(item.getQuantity()));
            subtotal = subtotal.add(itemTotal);
            itemCount += item.getQuantity();
        }

        cart.setSubtotal(subtotal);
        cart.setItemCount(itemCount);
    }

    private CartResponse mapToCartResponse(Cart cart) {
        CartResponse response = new CartResponse();
        response.setId(cart.getId());
        response.setSubtotal(cart.getSubtotal());
        response.setItemCount(cart.getItemCount());
        response.setUpdatedAt(cart.getUpdatedAt());

        List<CartResponse.CartItemResponse> itemResponses = cart.getItems().stream()
                .map(item -> {
                    Product product = productRepository.findById(item.getProductId()).orElse(null);

                    CartResponse.CartItemResponse itemResponse = new CartResponse.CartItemResponse();
                    itemResponse.setId(item.getProductId());
                    itemResponse.setProductId(item.getProductId());
                    itemResponse.setQuantity(item.getQuantity());
                    itemResponse.setSelectedColor(item.getSelectedColor());
                    itemResponse.setSelectedSize(item.getSelectedSize());
                    itemResponse.setPrice(item.getPrice());
                    itemResponse.setSubtotal(item.getPrice().multiply(BigDecimal.valueOf(item.getQuantity())));
                    itemResponse.setAddedAt(item.getAddedAt());

                    if (product != null) {
                        CartResponse.ProductInfo productInfo = new CartResponse.ProductInfo();
                        productInfo.setId(product.getId());
                        productInfo.setName(product.getName());
                        productInfo.setSlug(product.getSlug());
                        productInfo.setPrice(product.getPrice());
                        productInfo.setImages(product.getImages().stream()
                                .map(img -> img.getUrl())
                                .collect(Collectors.toList()));
                        productInfo.setStock(product.getStock());
                        productInfo.setStatus(product.getStatus().name());
                        productInfo.setInStock(product.getStock() > 0);
                        itemResponse.setProduct(productInfo);
                    }

                    return itemResponse;
                })
                .collect(Collectors.toList());

        response.setItems(itemResponses);
        return response;
    }

    private boolean equals(String str1, String str2) {
        if (str1 == null && str2 == null) return true;
        if (str1 == null || str2 == null) return false;
        return str1.equals(str2);
    }
}

