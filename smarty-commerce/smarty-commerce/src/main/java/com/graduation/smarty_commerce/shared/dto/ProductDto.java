package com.graduation.smarty_commerce.shared.dto;

import com.graduation.smarty_commerce.io.Entity.CartItemEntity;
import com.graduation.smarty_commerce.io.Entity.CategoryEntity;
import com.graduation.smarty_commerce.io.Entity.OrderItemEntity;
import jakarta.persistence.*;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.Collection;
import java.util.List;

public class ProductDto implements Serializable {

    private static final long serialVersionUID = 5313493413859549877L;


    private long id;

    private String productId;

    private String productName;

    private java.math.BigDecimal price;

    private int stock;

    private String imageUrl;

    private String description;

    private Collection<CategoryDto> category;

    private List<OrderItemDto> orderItems;

    private List<CartItemDto> cartItems;


    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public String getProductName() {
        return productName;
    }

    public void setProductName(String productName) {
        this.productName = productName;
    }

    public BigDecimal getPrice() {
        return price;
    }

    public void setPrice(BigDecimal price) {
        this.price = price;
    }

    public int getStock() {
        return stock;
    }

    public void setStock(int stock) {
        this.stock = stock;
    }

    public String getImageUrl() {
        return imageUrl;
    }

    public void setImageUrl(String imageUrl) {
        this.imageUrl = imageUrl;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public Collection<CategoryDto> getCategory() {
        return category;
    }

    public void setCategory(Collection<CategoryDto> category) {
        this.category = category;
    }

    public List<OrderItemDto> getOrderItems() {
        return orderItems;
    }

    public void setOrderItems(List<OrderItemDto> orderItems) {
        this.orderItems = orderItems;
    }

    public List<CartItemDto> getCartItems() {
        return cartItems;
    }

    public void setCartItems(List<CartItemDto> cartItems) {
        this.cartItems = cartItems;
    }

    public String getProductId() {
        return productId;
    }

    public void setProductId(String productId) {
        this.productId = productId;
    }
}

