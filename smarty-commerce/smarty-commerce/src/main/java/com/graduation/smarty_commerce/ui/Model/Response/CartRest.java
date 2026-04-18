package com.graduation.smarty_commerce.ui.Model.Response;

import java.util.List;

public class CartRest {
    private String cartId;
    private UserRest user;
    private List<CartItemRest> items;

    public String getCartId() {
        return cartId;
    }

    public void setCartId(String cartId) {
        this.cartId = cartId;
    }

    public UserRest getUser() {
        return user;
    }

    public void setUser(UserRest user) {
        this.user = user;
    }

    public List<CartItemRest> getCartItems() {
        return items;
    }

    public void setCartItems(List<CartItemRest> cartItems) {
        this.items = cartItems;
    }
}
