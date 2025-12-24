package com.tcon.ecom.controller;

import com.tcon.ecom.dto.request.AddressRequest;
import com.tcon.ecom.dto.response.AddressResponse;
import com.tcon.ecom.dto.response.ApiResponse;
import com.tcon.ecom.service.AddressService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/users/me/addresses")
@Tag(name = "Address Management", description = "User address management APIs")
@SecurityRequirement(name = "Bearer Authentication")
@RequiredArgsConstructor
public class AddressController {

    private final AddressService addressService;

    @GetMapping
    @Operation(summary = "Get all user addresses")
    public ResponseEntity<ApiResponse<List<AddressResponse>>> getUserAddresses(
            @AuthenticationPrincipal UserDetails userDetails) {
        List<AddressResponse> addresses = addressService.getUserAddresses(userDetails.getUsername());
        return ResponseEntity.ok(ApiResponse.success(addresses));
    }

    @PostMapping
    @Operation(summary = "Add new address")
    public ResponseEntity<ApiResponse<AddressResponse>> addAddress(
            @AuthenticationPrincipal UserDetails userDetails,
            @Valid @RequestBody AddressRequest request) {
        AddressResponse address = addressService.addAddress(userDetails.getUsername(), request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("Address added successfully", address));
    }

    @PatchMapping("/{addressId}")
    @Operation(summary = "Update address")
    public ResponseEntity<ApiResponse<AddressResponse>> updateAddress(
            @AuthenticationPrincipal UserDetails userDetails,
            @PathVariable String addressId,
            @Valid @RequestBody AddressRequest request) {
        AddressResponse address = addressService.updateAddress(userDetails.getUsername(), addressId, request);
        return ResponseEntity.ok(ApiResponse.success("Address updated successfully", address));
    }

    @DeleteMapping("/{addressId}")
    @Operation(summary = "Delete address")
    public ResponseEntity<ApiResponse<String>> deleteAddress(
            @AuthenticationPrincipal UserDetails userDetails,
            @PathVariable String addressId) {
        addressService.deleteAddress(userDetails.getUsername(), addressId);
        return ResponseEntity.ok(ApiResponse.success("Address deleted successfully", null));
    }

    @PatchMapping("/{addressId}/default")
    @Operation(summary = "Set address as default")
    public ResponseEntity<ApiResponse<String>> setDefaultAddress(
            @AuthenticationPrincipal UserDetails userDetails,
            @PathVariable String addressId) {
        addressService.setDefaultAddress(userDetails.getUsername(), addressId);
        return ResponseEntity.ok(ApiResponse.success("Default address updated", null));
    }
}

