package com.graduation.smarty_commerce.Service;

import com.graduation.smarty_commerce.shared.dto.AddressDto;

import java.util.List;

public interface AddressService {

    public List<AddressDto> getAddresses(String userId);
    public AddressDto getAddress(String addressId);
    public AddressDto createAddress(String userId ,AddressDto addressDto);
    public AddressDto updateAddress(String addressId, AddressDto addressDto);
    public void deleteAddress(String addressId);
}
