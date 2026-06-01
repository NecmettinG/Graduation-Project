package com.graduation.smarty_commerce.Service.impl;

import com.graduation.smarty_commerce.Exceptions.UserServiceException;
import com.graduation.smarty_commerce.Service.WishlistService;
import com.graduation.smarty_commerce.io.Entity.ProductEntity;
import com.graduation.smarty_commerce.io.Entity.UserEntity;
import com.graduation.smarty_commerce.io.Repository.ProductRepository;
import com.graduation.smarty_commerce.io.Repository.UserRepository;
import com.graduation.smarty_commerce.shared.dto.ProductDto;
import com.graduation.smarty_commerce.ui.Model.Response.ErrorMessages;
import org.modelmapper.ModelMapper;
import org.modelmapper.TypeToken;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;

@Service
public class WishlistServiceImpl implements WishlistService {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private ProductRepository productRepository;

    @Override
    public List<ProductDto> getWishlist(String userId) {
        UserEntity userEntity = userRepository.findByUserId(userId);
        if (userEntity == null) throw new UserServiceException(ErrorMessages.NO_RECORD_FOUND.getErrorMessage());

        List<ProductEntity> wishlist = userEntity.getWishlist();
        if (wishlist == null) {
            wishlist = new ArrayList<>();
        }

        ModelMapper modelMapper = new ModelMapper();
        Type listType = new TypeToken<List<ProductDto>>() {}.getType();
        return modelMapper.map(wishlist, listType);
    }

    @Override
    public void addProductToWishlist(String userId, String productId) {
        UserEntity userEntity = userRepository.findByUserId(userId);
        if (userEntity == null) throw new UserServiceException(ErrorMessages.NO_RECORD_FOUND.getErrorMessage());

        ProductEntity productEntity = productRepository.findByProductId(productId);
        if (productEntity == null) throw new UserServiceException(ErrorMessages.NO_RECORD_FOUND.getErrorMessage() + ": Product");

        List<ProductEntity> wishlist = userEntity.getWishlist();
        if (wishlist == null) {
            wishlist = new ArrayList<>();
            userEntity.setWishlist(wishlist);
        }

        // Avoid duplicates
        boolean exists = wishlist.stream().anyMatch(product -> product.getProductId().equals(productId));
        if (!exists) {
            wishlist.add(productEntity);
            userRepository.save(userEntity);
        }
    }

    @Override
    public void removeProductFromWishlist(String userId, String productId) {
        UserEntity userEntity = userRepository.findByUserId(userId);
        if (userEntity == null) throw new UserServiceException(ErrorMessages.NO_RECORD_FOUND.getErrorMessage());

        List<ProductEntity> wishlist = userEntity.getWishlist();
        if (wishlist != null) {
            wishlist.removeIf(product -> product.getProductId().equals(productId));
            userRepository.save(userEntity);
        }
    }
}

