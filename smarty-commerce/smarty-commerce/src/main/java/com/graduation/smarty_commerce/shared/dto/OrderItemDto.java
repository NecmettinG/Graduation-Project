package com.graduation.smarty_commerce.shared.dto;

import com.graduation.smarty_commerce.io.Entity.OrderEntity;
import com.graduation.smarty_commerce.io.Entity.ProductEntity;
import jakarta.persistence.*;

import java.io.Serializable;
import java.math.BigDecimal;

public class OrderItemDto implements Serializable {

    private static final long serialVersionUID = 1231295792623135497L;


    private long id;

    private String orderItemId;

    private ProductDto product;


    private OrderDto order;


    private int quantity;


    private java.math.BigDecimal unitPrice;


    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public ProductDto getProduct() {
        return product;
    }

    public void setProduct(ProductDto product) {
        this.product = product;
    }

    public OrderDto getOrder() {
        return order;
    }

    public void setOrder(OrderDto order) {
        this.order = order;
    }

    public int getQuantity() {
        return quantity;
    }

    public void setQuantity(int quantity) {
        this.quantity = quantity;
    }

    public BigDecimal getUnitPrice() {
        return unitPrice;
    }

    public void setUnitPrice(BigDecimal unitPrice) {
        this.unitPrice = unitPrice;
    }

    public String getOrderItemId() {
        return orderItemId;
    }

    public void setOrderItemId(String orderItemId) {
        this.orderItemId = orderItemId;
    }
}

