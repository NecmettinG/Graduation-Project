package com.graduation.smarty_commerce.Service.impl;

import com.graduation.smarty_commerce.Exceptions.CartServiceException;
import com.graduation.smarty_commerce.Service.CartService;
import com.graduation.smarty_commerce.io.Entity.CartEntity;
import com.graduation.smarty_commerce.io.Entity.CartItemEntity;
import com.graduation.smarty_commerce.io.Entity.ProductEntity;
import com.graduation.smarty_commerce.io.Entity.UserEntity;
import com.graduation.smarty_commerce.io.Repository.CartItemRepository;
import com.graduation.smarty_commerce.io.Repository.CartRepository;
import com.graduation.smarty_commerce.io.Repository.ProductRepository;
import com.graduation.smarty_commerce.io.Repository.UserRepository;
import com.graduation.smarty_commerce.shared.Utils;
import com.graduation.smarty_commerce.shared.dto.CartDto;
import com.graduation.smarty_commerce.shared.dto.CartItemDto;
import com.graduation.smarty_commerce.ui.Model.Request.CartItemRequestModel;
import com.graduation.smarty_commerce.ui.Model.Response.ErrorMessages;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Optional;

@Service
public class CartServiceImpl implements CartService {

    @Autowired
    CartRepository cartRepository;

    @Autowired
    CartItemRepository cartItemRepository;

    @Autowired
    UserRepository userRepository;

    @Autowired
    ProductRepository productRepository;

    @Autowired
    Utils utils;

    @Override
    public CartDto getCartByUserId(String userId) {
        CartEntity cartEntity = getOrCreateCart(userId);
        return new ModelMapper().map(cartEntity, CartDto.class);
    }

    @Override
    public CartDto addCartItem(String userId, CartItemDto cartItemDetails) {
        CartEntity cartEntity = getOrCreateCart(userId);
        
        String productId = cartItemDetails.getProduct() != null ? cartItemDetails.getProduct().getProductId() : null;
        ProductEntity productEntity = productRepository.findByProductId(productId);
        if (productEntity == null) throw new CartServiceException(ErrorMessages.PRODUCT_NOT_FOUND.getErrorMessage());

        CartItemEntity cartItemEntity = null;
        if (cartEntity.getCartItems() == null) {
            cartEntity.setCartItems(new ArrayList<>());
        }
        
        for (CartItemEntity item : cartEntity.getCartItems()) {
            if (item.getProduct().getProductId().equals(productEntity.getProductId())) {
                cartItemEntity = item;
                break;
            }
        }

        if (cartItemEntity != null) {
            cartItemEntity.setQuantity(cartItemEntity.getQuantity() + cartItemDetails.getQuantity());
        } else {
            cartItemEntity = new CartItemEntity();
            cartItemEntity.setCartItemId(utils.generateId(30));
            cartItemEntity.setProduct(productEntity);
            cartItemEntity.setCart(cartEntity);
            cartItemEntity.setQuantity(cartItemDetails.getQuantity());
            cartEntity.getCartItems().add(cartItemEntity);
        }

        recalculateTotal(cartEntity);
        CartEntity savedCart = cartRepository.save(cartEntity);
        return new ModelMapper().map(savedCart, CartDto.class);
    }

    @Override
    public CartDto updateCartItem(String userId, String cartItemId, CartItemDto cartItemDetails) {
        CartEntity cartEntity = getOrCreateCart(userId);
        
        CartItemEntity cartItemEntity = null;
        for (CartItemEntity item : cartEntity.getCartItems()) {
            if (item.getCartItemId().equals(cartItemId)) {
                cartItemEntity = item;
                break;
            }
        }
        
        if (cartItemEntity == null) throw new CartServiceException(ErrorMessages.CART_ITEM_NOT_FOUND.getErrorMessage());
        
        cartItemEntity.setQuantity(cartItemDetails.getQuantity());
        recalculateTotal(cartEntity);
        
        CartEntity savedCart = cartRepository.save(cartEntity);
        return new ModelMapper().map(savedCart, CartDto.class);
    }

    @Override
    public void removeCartItem(String userId, String cartItemId) {
        CartEntity cartEntity = getOrCreateCart(userId);
        
        CartItemEntity itemToRemove = null;
        for (CartItemEntity item : cartEntity.getCartItems()) {
            if (item.getCartItemId().equals(cartItemId)) {
                itemToRemove = item;
                break;
            }
        }
        
        if (itemToRemove == null) throw new CartServiceException(ErrorMessages.CART_ITEM_NOT_FOUND.getErrorMessage());
        
        cartEntity.getCartItems().remove(itemToRemove);
        cartItemRepository.delete(itemToRemove);
        
        recalculateTotal(cartEntity);
        cartRepository.save(cartEntity);
    }

    @Override
    public void clearCart(String userId) {
        CartEntity cartEntity = getOrCreateCart(userId);
        
        cartItemRepository.deleteAll(cartEntity.getCartItems());
        cartEntity.getCartItems().clear();
        
        recalculateTotal(cartEntity);
        cartRepository.save(cartEntity);
    }

    private CartEntity getOrCreateCart(String userId) {
        UserEntity userEntity = userRepository.findByUserId(userId);
        if (userEntity == null) throw new CartServiceException(ErrorMessages.USER_NOT_FOUND.getErrorMessage());

        CartEntity cartEntity = cartRepository.findByUserUserId(userId);
        if (cartEntity == null) {
            cartEntity = new CartEntity();
            cartEntity.setCartId(utils.generateId(30));
            cartEntity.setUser(userEntity);
            cartEntity.setCurrentTotal(BigDecimal.ZERO);
            cartEntity.setCartItems(new ArrayList<>());
            cartEntity = cartRepository.save(cartEntity);
        }
        return cartEntity;
    }

    private void recalculateTotal(CartEntity cartEntity) {
        BigDecimal total = BigDecimal.ZERO;
        if (cartEntity.getCartItems() != null) {
            for (CartItemEntity item : cartEntity.getCartItems()) {
                total = total.add(item.getProduct().getPrice().multiply(new BigDecimal(item.getQuantity())));
            }
        }
        cartEntity.setCurrentTotal(total);
    }
}
