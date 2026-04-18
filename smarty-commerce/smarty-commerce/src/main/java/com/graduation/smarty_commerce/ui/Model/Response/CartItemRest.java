package com.graduation.smarty_commerce.ui.Model.Response;

public class CartItemRest {
    private String cartItemId;
    private ProductRest product;
    private int quantity;

    public String getCartItemId() {
        return cartItemId;
    }

    public void setCartItemId(String cartItemId) {
        this.cartItemId = cartItemId;
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
}
