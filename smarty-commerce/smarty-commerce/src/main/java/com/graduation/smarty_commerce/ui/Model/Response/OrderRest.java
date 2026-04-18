package com.graduation.smarty_commerce.ui.Model.Response;

import com.graduation.smarty_commerce.shared.OrderStatus;

import java.math.BigDecimal;
import java.util.Date;
import java.util.List;

public class OrderRest {
    private String orderId;
    private Date orderDate;
    private OrderStatus orderStatus;
    private BigDecimal totalAmount;
    private String shippingAddress;
    private UserRest user;
    private List<OrderItemRest> orderItems;

    public String getOrderId() {
        return orderId;
    }

    public void setOrderId(String orderId) {
        this.orderId = orderId;
    }

    public Date getOrderDate() {
        return orderDate;
    }

    public void setOrderDate(Date orderDate) {
        this.orderDate = orderDate;
    }

    public OrderStatus getOrderStatus() {
        return orderStatus;
    }

    public void setOrderStatus(OrderStatus orderStatus) {
        this.orderStatus = orderStatus;
    }

    public BigDecimal getTotalAmount() {
        return totalAmount;
    }

    public void setTotalAmount(BigDecimal totalAmount) {
        this.totalAmount = totalAmount;
    }

    public String getShippingAddress() {
        return shippingAddress;
    }

    public void setShippingAddress(String shippingAddress) {
        this.shippingAddress = shippingAddress;
    }

    public UserRest getUser() {
        return user;
    }

    public void setUser(UserRest user) {
        this.user = user;
    }

    public List<OrderItemRest> getOrderItems() {
        return orderItems;
    }

    public void setOrderItems(List<OrderItemRest> orderItems) {
        this.orderItems = orderItems;
    }
}
