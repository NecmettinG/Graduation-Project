package com.graduation.smarty_commerce.Service;

import com.graduation.smarty_commerce.shared.dto.ProductDto;

import java.util.List;

public interface WishlistService {
    List<ProductDto> getWishlist(String userId);
    void addProductToWishlist(String userId, String productId);
    void removeProductFromWishlist(String userId, String productId);
}
