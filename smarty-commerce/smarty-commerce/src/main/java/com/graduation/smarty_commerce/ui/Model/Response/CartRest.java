package com.graduation.smarty_commerce.ui.Model.Response;

import java.math.BigDecimal;
import java.util.List;

public class CartRest {
    private String cartId;
    private BigDecimal currentTotal;
    private UserRest user;
    private List<CartItemRest> items;

    public String getCartId() {
        return cartId;
    }

    public void setCartId(String cartId) {
        this.cartId = cartId;
    }

    public BigDecimal getCurrentTotal() {
        return currentTotal;
    }

    public void setCurrentTotal(BigDecimal currentTotal) {
        this.currentTotal = currentTotal;
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
