package com.graduation.smarty_commerce.Service;

import com.graduation.smarty_commerce.shared.dto.CartDto;
import com.graduation.smarty_commerce.shared.dto.CartItemDto;
import com.graduation.smarty_commerce.ui.Model.Request.CartItemRequestModel;

public interface CartService {
    CartDto getCartByUserId(String userId);
    CartDto addCartItem(String userId, CartItemRequestModel cartItemDetails);
    CartDto updateCartItem(String userId, String cartItemId, CartItemRequestModel cartItemDetails);
    void removeCartItem(String userId, String cartItemId);
    void clearCart(String userId);
}
