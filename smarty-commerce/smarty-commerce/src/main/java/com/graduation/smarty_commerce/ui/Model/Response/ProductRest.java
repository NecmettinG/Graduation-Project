package com.graduation.smarty_commerce.ui.Model.Response;

import java.math.BigDecimal;

public class ProductRest {
    private String productId;
    private String productName;
    private BigDecimal price;
    private int stock;
    private String imageUrl;
    private String description;
    private CategoryRest category;

    public String getProductId() {
        return productId;
    }

    public void setProductId(String productId) {
        this.productId = productId;
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

    public CategoryRest getCategory() {
        return category;
    }

    public void setCategory(CategoryRest category) {
        this.category = category;
    }
}
