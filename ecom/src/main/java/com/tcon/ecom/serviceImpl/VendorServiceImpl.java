package com.tcon.ecom.serviceImpl;

import com.tcon.ecom.dto.request.*;
import com.tcon.ecom.dto.response.*;
import com.tcon.ecom.exception.BadRequestException;
import com.tcon.ecom.exception.EmailAlreadyExistsException;
import com.tcon.ecom.model.User;
import com.tcon.ecom.model.Vendor;
import com.tcon.ecom.model.enums.UserRole;
import com.tcon.ecom.model.enums.UserStatus;
import com.tcon.ecom.model.enums.VendorStatus;
import com.tcon.ecom.repository.*;
import com.tcon.ecom.service.EmailService;
import com.tcon.ecom.service.VendorService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.modelmapper.ModelMapper;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class VendorServiceImpl implements VendorService {

    private final UserRepository userRepository;
    private final ProductRepository productRepository;
    private final OrderRepository orderRepository;
    private final CouponRepository couponRepository;
    private final VendorRepository vendorRepository;
    private final PasswordEncoder passwordEncoder;
    private final EmailService emailService;
    private final ModelMapper modelMapper;

    @Override
    @Transactional
    public VendorResponse registerVendor(VendorRegisterRequest request) {
        log.info("Registering new vendor with email: {}", request.getEmail());

        // Check if email already exists
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new EmailAlreadyExistsException(request.getEmail());
        }

        // Check if vendor with this email already exists
        if (vendorRepository.existsByEmail(request.getEmail())) {
            throw new BadRequestException("A vendor application with this email already exists");
        }

        // Check if tax ID already exists
        if (vendorRepository.existsByTaxId(request.getTaxId())) {
            throw new BadRequestException("A vendor with this Tax ID already exists");
        }

        // Create User account
        User user = new User();
        user.setEmail(request.getEmail());
        user.setPassword(passwordEncoder.encode(request.getPassword()));
        user.setFirstName(request.getContactPerson().split(" ")[0]);
        user.setLastName(request.getContactPerson().contains(" ") ?
            request.getContactPerson().substring(request.getContactPerson().indexOf(" ") + 1) :
            request.getContactPerson());
        user.setPhone(request.getPhone());
        user.setRole(UserRole.VENDOR);
        user.setStatus(UserStatus.PENDING); // Pending until vendor is approved
        user.setEmailVerified(false);
        user.setLoginAttempts(0);

        // Generate email verification token
        String verificationToken = UUID.randomUUID().toString();
        user.setEmailVerificationToken(verificationToken);
        user.setEmailVerificationExpiry(LocalDateTime.now().plusHours(24));

        // Save user
        user = userRepository.save(user);
        log.info("User account created with ID: {}", user.getId());

        // Create Vendor profile
        Vendor vendor = new Vendor();
        vendor.setUserId(user.getId());
        vendor.setEmail(request.getEmail());
        vendor.setStoreName(request.getStoreName());
        vendor.setContactPerson(request.getContactPerson());
        vendor.setPhone(request.getPhone());
        vendor.setBusinessType(request.getBusinessType());
        vendor.setTaxId(request.getTaxId());
        vendor.setStatus(VendorStatus.PENDING);

        // Set business address if provided
        if (request.getBusinessAddress() != null) {
            Vendor.BusinessAddress address = new Vendor.BusinessAddress();
            address.setStreet(request.getBusinessAddress().getStreet());
            address.setCity(request.getBusinessAddress().getCity());
            address.setState(request.getBusinessAddress().getState());
            address.setZipCode(request.getBusinessAddress().getZipCode());
            address.setCountry(request.getBusinessAddress().getCountry());
            vendor.setBusinessAddress(address);
        }

        // Save vendor
        vendor = vendorRepository.save(vendor);
        log.info("Vendor profile created with ID: {}", vendor.getId());

        // Update user with vendor profile reference
        user.setVendorProfile(vendor.getId());
        userRepository.save(user);

        // Send verification email
        try {
            log.info("📧 Sending verification email to: {}", user.getEmail());
            emailService.sendVerificationEmail(user.getEmail(), verificationToken, user.getFirstName());
            log.info("✅ Verification email sent successfully!");
        } catch (Exception e) {
            log.error("❌ Failed to send verification email", e);
        }

        log.info("╔════════════════════════════════════════════════════════════════════════════╗");
        log.info("║              REGISTRATION SUMMARY                                          ║");
        log.info("╠════════════════════════════════════════════════════════════════════════════╣");
        log.info("║ Email: {}                                     ", user.getEmail());
        log.info("║ User ID: {}                                  ", user.getId());
        log.info("║ Vendor ID: {}                                ", vendor.getId());
        log.info("║ User Status: {}                                                 ", user.getStatus());
        log.info("║ Vendor Status: {}                                               ", vendor.getStatus());
        log.info("║ Email Verified: {}                                                     ", user.getEmailVerified());
        log.info("║ Can Login: NO (Email verification required)                               ║");
        log.info("║                                                                            ║");
        log.info("║ ⏳ NEXT STEP: Check email and click verification link                      ║");
        log.info("╚════════════════════════════════════════════════════════════════════════════╝");

        log.info("Vendor registration completed successfully for: {}", request.getEmail());

        // Build response
        VendorResponse response = VendorResponse.builder()
                .id(vendor.getId())
                .userId(user.getId())
                .email(vendor.getEmail())
                .status(vendor.getStatus().name())
                .storeName(vendor.getStoreName())
                .contactPerson(vendor.getContactPerson())
                .phone(vendor.getPhone())
                .businessType(vendor.getBusinessType())
                .taxId(vendor.getTaxId())
                .createdAt(vendor.getCreatedAt())
                .build();

        // Set business address if present
        if (vendor.getBusinessAddress() != null) {
            VendorResponse.BusinessAddress addressResponse = VendorResponse.BusinessAddress.builder()
                    .street(vendor.getBusinessAddress().getStreet())
                    .city(vendor.getBusinessAddress().getCity())
                    .state(vendor.getBusinessAddress().getState())
                    .zipCode(vendor.getBusinessAddress().getZipCode())
                    .country(vendor.getBusinessAddress().getCountry())
                    .build();
            response.setBusinessAddress(addressResponse);
        }

        log.info("Vendor registration completed successfully for: {}", request.getEmail());
        return response;
    }

    @Override
    public VendorDashboardStats getDashboardStats(String email, String period) {
        // TODO: Implement dashboard stats
        throw new BadRequestException("Dashboard stats not yet implemented. Please implement this feature.");
    }

    @Override
    public VendorAnalytics getAnalytics(String email, LocalDate startDate, LocalDate endDate, String groupBy) {
        // TODO: Implement analytics
        throw new BadRequestException("Analytics not yet implemented. Please implement this feature.");
    }

    @Override
    public Page<ProductListResponse> getVendorProducts(String email, String status, String category, String search, int page, int limit) {
        // TODO: Implement vendor product listing
        log.warn("getVendorProducts called but not yet implemented");
        return new PageImpl<>(new ArrayList<>(), PageRequest.of(page, limit), 0);
    }

    @Override
    public ProductDetailResponse createProduct(String email, CreateProductRequest request, List<MultipartFile> images) {
        // TODO: Implement product creation
        throw new BadRequestException("Product creation not yet implemented. Please implement this feature.");
    }

    @Override
    public ProductDetailResponse updateProduct(String email, String productId, UpdateProductRequest request) {
        // TODO: Implement product update
        throw new BadRequestException("Product update not yet implemented. Please implement this feature.");
    }

    @Override
    public void deleteProduct(String email, String productId) {
        // TODO: Implement product deletion
        throw new BadRequestException("Product deletion not yet implemented. Please implement this feature.");
    }

    @Override
    public Page<OrderResponse> getVendorOrders(String email, String status, LocalDate startDate, LocalDate endDate, int page, int limit) {
        // TODO: Implement vendor order listing
        log.warn("getVendorOrders called but not yet implemented");
        return new PageImpl<>(new ArrayList<>(), PageRequest.of(page, limit), 0);
    }

    @Override
    public void updateOrderStatus(String email, String orderNumber, UpdateOrderStatusRequest request) {
        // TODO: Implement order status update
        throw new BadRequestException("Order status update not yet implemented. Please implement this feature.");
    }

    @Override
    public CouponResponse createCoupon(String email, CreateCouponRequest request) {
        // TODO: Implement coupon creation
        throw new BadRequestException("Coupon creation not yet implemented. Please implement this feature.");
    }

    @Override
    public List<CouponResponse> getVendorCoupons(String email, String status) {
        // TODO: Implement coupon listing
        log.warn("getVendorCoupons called but not yet implemented");
        return new ArrayList<>();
    }
}

