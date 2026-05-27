package com.graduation.smarty_commerce.ui.Model.Response;

import java.math.BigDecimal;

public class OrderItemRest {
    private String orderItemId;
    private ProductRest product;
    private int quantity;
    private BigDecimal unitPrice;

    public String getOrderItemId() {
        return orderItemId;
    }

    public void setOrderItemId(String orderItemId) {
        this.orderItemId = orderItemId;
    }

    public ProductRest getProduct() {
        return product;
    }

    public void setProduct(ProductRest product) {
        this.product = product;
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
}
