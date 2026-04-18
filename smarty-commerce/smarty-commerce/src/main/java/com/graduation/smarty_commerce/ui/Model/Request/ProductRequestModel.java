package com.graduation.smarty_commerce.ui.Model.Request;

import com.graduation.smarty_commerce.shared.dto.CategoryDto;

import java.util.Collection;
import java.util.List;

public class ProductRequestModel {

    private String productName;

    private java.math.BigDecimal price;

    private int stock;

    private String imageUrl;

    private String description;

    private List<String> categoryIds;

    public String getProductName() {
        return productName;
    }

    public void setProductName(String productName) {
        this.productName = productName;
    }

    public java.math.BigDecimal getPrice() {
        return price;
    }

    public void setPrice(java.math.BigDecimal price) {
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

    public List<String> getCategoryIds() {
        return categoryIds;
    }

    public void setCategoryIds(List<String> categoryIds) {
        this.categoryIds = categoryIds;
    }
}
