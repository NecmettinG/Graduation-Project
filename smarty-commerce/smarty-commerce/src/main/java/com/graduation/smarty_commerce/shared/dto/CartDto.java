package com.graduation.smarty_commerce.shared.dto;

import com.graduation.smarty_commerce.io.Entity.CartItemEntity;
import com.graduation.smarty_commerce.io.Entity.UserEntity;
import jakarta.persistence.*;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.List;

public class CartDto implements Serializable {

    private static final long serialVersionUID = 1231295114321564897L;

    private long id;

    private String cartId;

    private java.math.BigDecimal currentTotal;

    private List<CartItemDto> cartItems;

    private UserDto user;

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public BigDecimal getCurrentTotal() {
        return currentTotal;
    }

    public void setCurrentTotal(BigDecimal currentTotal) {
        this.currentTotal = currentTotal;
    }

    public List<CartItemDto> getCartItems() {
        return cartItems;
    }

    public void setCartItems(List<CartItemDto> cartItems) {
        this.cartItems = cartItems;
    }

    public UserDto getUser() {
        return user;
    }

    public void setUser(UserDto user) {
        this.user = user;
    }

    public String getCartId() {
        return cartId;
    }

    public void setCartId(String cartId) {
        this.cartId = cartId;
    }
}

