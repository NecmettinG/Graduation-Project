package com.graduation.smarty_commerce.shared.dto;

import com.graduation.smarty_commerce.io.Entity.ProductEntity;
import jakarta.persistence.*;

import java.io.Serializable;
import java.util.Collection;

public class CategoryDto implements Serializable {

    private static final long serialVersionUID = 5313493413852318649L;

    private long id;

    private String categoryId;

    private String categoryName;

    private Collection<ProductDto> products;

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public String getCategoryName() {
        return categoryName;
    }

    public void setCategoryName(String categoryName) {
        this.categoryName = categoryName;
    }

    public Collection<ProductDto> getProducts() {
        return products;
    }

    public void setProducts(Collection<ProductDto> products) {
        this.products = products;
    }

    public String getCategoryId() {
        return categoryId;
    }

    public void setCategoryId(String categoryId) {
        this.categoryId = categoryId;
    }
}

