package com.graduation.smarty_commerce.Service;

import com.graduation.smarty_commerce.shared.dto.CartDto;
import com.graduation.smarty_commerce.shared.dto.CartItemDto;

public interface CartService {
    CartDto getCartByUserId(String userId);
    CartDto addCartItem(String userId, CartItemDto cartItemDetails);
    CartDto updateCartItem(String userId, String cartItemId, CartItemDto cartItemDetails);
    void removeCartItem(String userId, String cartItemId);
    void clearCart(String userId);
}
