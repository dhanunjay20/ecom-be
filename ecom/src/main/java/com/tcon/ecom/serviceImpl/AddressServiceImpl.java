package com.tcon.ecom.serviceImpl;

import com.tcon.ecom.dto.request.AddressRequest;
import com.tcon.ecom.dto.response.AddressResponse;
import com.tcon.ecom.exception.BadRequestException;
import com.tcon.ecom.exception.ResourceNotFoundException;
import com.tcon.ecom.model.Address;
import com.tcon.ecom.model.User;
import com.tcon.ecom.repository.AddressRepository;
import com.tcon.ecom.repository.UserRepository;
import com.tcon.ecom.service.AddressService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class AddressServiceImpl implements AddressService {

    private final AddressRepository addressRepository;
    private final UserRepository userRepository;

    @Override
    public List<AddressResponse> getUserAddresses(String email) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        List<Address> addresses = addressRepository.findByUserId(user.getId());
        return addresses.stream()
                .map(this::mapToAddressResponse)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public AddressResponse addAddress(String email, AddressRequest request) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        // If this is set as default, unset other default addresses
        if (request.getIsDefault()) {
            addressRepository.findByUserIdAndIsDefaultTrue(user.getId())
                    .ifPresent(defaultAddress -> {
                        defaultAddress.setIsDefault(false);
                        addressRepository.save(defaultAddress);
                    });
        }

        // If this is the first address, make it default
        Long addressCount = addressRepository.countByUserId(user.getId());
        boolean makeDefault = addressCount == 0 || request.getIsDefault();

        Address address = new Address();
        address.setUserId(user.getId());
        address.setType(request.getType());
        address.setFullName(request.getFullName());
        address.setStreet(request.getStreet());
        address.setApartment(request.getApartment());
        address.setCity(request.getCity());
        address.setState(request.getState());
        address.setZipCode(request.getZipCode());
        address.setCountry(request.getCountry());
        address.setPhone(request.getPhone());
        address.setInstructions(request.getInstructions());
        address.setIsDefault(makeDefault);

        Address savedAddress = addressRepository.save(address);
        log.info("Address added for user: {}", email);

        return mapToAddressResponse(savedAddress);
    }

    @Override
    @Transactional
    public AddressResponse updateAddress(String email, String addressId, AddressRequest request) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        Address address = addressRepository.findById(addressId)
                .orElseThrow(() -> new ResourceNotFoundException("Address not found"));

        // Verify address belongs to user
        if (!address.getUserId().equals(user.getId())) {
            throw new BadRequestException("Address does not belong to user");
        }

        // Handle default address change
        if (request.getIsDefault() != null && request.getIsDefault() && !address.getIsDefault()) {
            addressRepository.findByUserIdAndIsDefaultTrue(user.getId())
                    .ifPresent(defaultAddress -> {
                        defaultAddress.setIsDefault(false);
                        addressRepository.save(defaultAddress);
                    });
        }

        // Update fields
        if (request.getType() != null) address.setType(request.getType());
        if (request.getFullName() != null) address.setFullName(request.getFullName());
        if (request.getStreet() != null) address.setStreet(request.getStreet());
        if (request.getApartment() != null) address.setApartment(request.getApartment());
        if (request.getCity() != null) address.setCity(request.getCity());
        if (request.getState() != null) address.setState(request.getState());
        if (request.getZipCode() != null) address.setZipCode(request.getZipCode());
        if (request.getCountry() != null) address.setCountry(request.getCountry());
        if (request.getPhone() != null) address.setPhone(request.getPhone());
        if (request.getInstructions() != null) address.setInstructions(request.getInstructions());
        if (request.getIsDefault() != null) address.setIsDefault(request.getIsDefault());

        Address updatedAddress = addressRepository.save(address);
        log.info("Address updated for user: {}", email);

        return mapToAddressResponse(updatedAddress);
    }

    @Override
    @Transactional
    public void deleteAddress(String email, String addressId) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        Address address = addressRepository.findById(addressId)
                .orElseThrow(() -> new ResourceNotFoundException("Address not found"));

        // Verify address belongs to user
        if (!address.getUserId().equals(user.getId())) {
            throw new BadRequestException("Address does not belong to user");
        }

        // Check if this is the only address
        Long addressCount = addressRepository.countByUserId(user.getId());
        if (addressCount == 1) {
            throw new BadRequestException("Cannot delete the only address");
        }

        // If deleting default address, set another as default
        if (address.getIsDefault()) {
            List<Address> otherAddresses = addressRepository.findByUserId(user.getId());
            otherAddresses.stream()
                    .filter(addr -> !addr.getId().equals(addressId))
                    .findFirst()
                    .ifPresent(newDefault -> {
                        newDefault.setIsDefault(true);
                        addressRepository.save(newDefault);
                    });
        }

        addressRepository.delete(address);
        log.info("Address deleted for user: {}", email);
    }

    @Override
    @Transactional
    public void setDefaultAddress(String email, String addressId) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        Address address = addressRepository.findById(addressId)
                .orElseThrow(() -> new ResourceNotFoundException("Address not found"));

        // Verify address belongs to user
        if (!address.getUserId().equals(user.getId())) {
            throw new BadRequestException("Address does not belong to user");
        }

        // Unset current default
        addressRepository.findByUserIdAndIsDefaultTrue(user.getId())
                .ifPresent(defaultAddress -> {
                    defaultAddress.setIsDefault(false);
                    addressRepository.save(defaultAddress);
                });

        // Set new default
        address.setIsDefault(true);
        addressRepository.save(address);
        log.info("Default address set for user: {}", email);
    }

    private AddressResponse mapToAddressResponse(Address address) {
        AddressResponse response = new AddressResponse();
        response.setId(address.getId());
        response.setType(address.getType() != null ? address.getType().name() : null);
        response.setIsDefault(address.getIsDefault());
        response.setFullName(address.getFullName());
        response.setStreet(address.getStreet());
        response.setApartment(address.getApartment());
        response.setCity(address.getCity());
        response.setState(address.getState());
        response.setZipCode(address.getZipCode());
        response.setCountry(address.getCountry());
        response.setPhone(address.getPhone());
        response.setInstructions(address.getInstructions());
        response.setCreatedAt(address.getCreatedAt());
        return response;
    }
}

