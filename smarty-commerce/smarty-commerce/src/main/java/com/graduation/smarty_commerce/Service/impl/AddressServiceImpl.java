package com.graduation.smarty_commerce.Service.impl;

import com.graduation.smarty_commerce.Exceptions.UserServiceException;
import com.graduation.smarty_commerce.Service.AddressService;
import com.graduation.smarty_commerce.io.Entity.AddressEntity;
import com.graduation.smarty_commerce.io.Entity.UserEntity;
import com.graduation.smarty_commerce.io.Repository.AddressRepository;
import com.graduation.smarty_commerce.io.Repository.UserRepository;
import com.graduation.smarty_commerce.shared.Utils;
import com.graduation.smarty_commerce.shared.dto.AddressDto;
import com.graduation.smarty_commerce.ui.Model.Response.ErrorMessages;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
public class AddressServiceImpl implements AddressService {

    private final UserRepository userRepository;
    
    private final AddressRepository addressRepository;

    private final Utils utils;

    @Autowired
    public AddressServiceImpl(UserRepository userRepository, AddressRepository addressRepository, Utils utils) {
        this.userRepository = userRepository;
        this.addressRepository = addressRepository;
        this.utils = utils;
    }

    @Override
    public List<AddressDto> getAddresses(String userId){

        List<AddressDto> returnValue = new ArrayList<>();

        UserEntity userEntity = userRepository.findByUserId(userId);

        if(userEntity == null) return returnValue;

        Iterable<AddressEntity> addresses = addressRepository.findAllByUserDetails(userEntity);

        ModelMapper modelMapper = new ModelMapper();

        for(AddressEntity addressEntity : addresses){

            returnValue.add(modelMapper.map(addressEntity, AddressDto.class));
        }

        return returnValue;
    }

    @Override
    public AddressDto getAddress(String addressId){

        AddressDto returnValue = null;

        AddressEntity addressEntity = addressRepository.findByAddressId(addressId);

        if(addressEntity != null){

            returnValue = new ModelMapper().map(addressEntity, AddressDto.class);
        }

        return returnValue;
    }

    @Override
    public AddressDto createAddress(String userId ,AddressDto addressDto){

        AddressDto returnValue = new AddressDto();

        ModelMapper modelMapper = new ModelMapper();

        UserEntity userEntity = userRepository.findByUserId(userId);

        AddressEntity addressEntity = modelMapper.map(addressDto, AddressEntity.class);

        String addressId = utils.generateId(30);

        if(addressRepository.findByAddressId(addressId) != null){

            throw new UserServiceException("Duplicated Address ID occured. Please try again.");
        }

        addressEntity.setAddressId(addressId);

        addressEntity.setUserDetails(userEntity);

        userEntity.getAddresses().add(addressEntity);

        AddressEntity savedAddress = addressRepository.save(addressEntity);

        returnValue = modelMapper.map(savedAddress, AddressDto.class);

        return returnValue;
    }

    @Override
    public AddressDto updateAddress(String addressId, AddressDto addressDto){

        AddressDto returnValue = new AddressDto();

        AddressEntity addressEntity = addressRepository.findByAddressId(addressId);

        if(addressEntity == null){

            throw new UserServiceException(ErrorMessages.NO_RECORD_FOUND.getErrorMessage());
        }

        addressEntity.setStreetName(addressDto.getStreetName());
        addressEntity.setCity(addressDto.getCity());
        addressEntity.setCountry(addressDto.getCountry());
        addressEntity.setType(addressDto.getType());
        addressEntity.setPostalCode(addressDto.getPostalCode());

        AddressEntity updatedAddressEntity = addressRepository.save(addressEntity);

        ModelMapper modelMapper = new ModelMapper();

        returnValue = modelMapper.map(updatedAddressEntity, AddressDto.class);

        return returnValue;
    }

    @Override
    public void deleteAddress(String addressId){

        AddressEntity addressEntity = addressRepository.findByAddressId(addressId);

        if(addressEntity == null){

            throw new UserServiceException(ErrorMessages.NO_RECORD_FOUND.getErrorMessage());
        }

        addressRepository.delete(addressEntity);
    }
}
