package com.tcon.ecom.service;

import com.tcon.ecom.dto.request.AddressRequest;
import com.tcon.ecom.dto.response.AddressResponse;

import java.util.List;

public interface AddressService {
    List<AddressResponse> getUserAddresses(String email);
    AddressResponse addAddress(String email, AddressRequest request);
    AddressResponse updateAddress(String email, String addressId, AddressRequest request);
    void deleteAddress(String email, String addressId);
    void setDefaultAddress(String email, String addressId);
}

