package com.graduation.smarty_commerce.shared.dto;

import com.graduation.smarty_commerce.io.Entity.CartEntity;
import com.graduation.smarty_commerce.io.Entity.ProductEntity;
import jakarta.persistence.*;

import java.io.Serializable;

public class CartItemDto implements Serializable {

    private static final long serialVersionUID = 1231295114815649874L;


    private long id;

    private String cartItemId;

    private CartDto cart;


    private ProductDto product;


    private int quantity;


    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public CartDto getCart() {
        return cart;
    }

    public void setCart(CartDto cart) {
        this.cart = cart;
    }

    public ProductDto getProduct() {
        return product;
    }

    public void setProduct(ProductDto product) {
        this.product = product;
    }

    public int getQuantity() {
        return quantity;
    }

    public void setQuantity(int quantity) {
        this.quantity = quantity;
    }

    public String getCartItemId() {
        return cartItemId;
    }

    public void setCartItemId(String cartItemId) {
        this.cartItemId = cartItemId;
    }
}

