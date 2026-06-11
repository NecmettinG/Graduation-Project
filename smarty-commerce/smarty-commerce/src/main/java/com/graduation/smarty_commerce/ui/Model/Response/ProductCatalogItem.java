package com.graduation.smarty_commerce.ui.Model.Response;

import java.math.BigDecimal;

/**
 * Lightweight DTO for the product catalog data-feed endpoint.
 * Contains only the fields needed by the recommendation service's
 * Content-Based Filtering component: category hierarchy, brand, and price.
 */
public class ProductCatalogItem {

    private String productId;
    private String categoryName;
    private String mainCategoryName;
    private String brand;
    private BigDecimal price;

    public ProductCatalogItem() {
    }

    public ProductCatalogItem(String productId, String categoryName, String mainCategoryName, String brand, BigDecimal price) {
        this.productId = productId;
        this.categoryName = categoryName;
        this.mainCategoryName = mainCategoryName;
        this.brand = brand;
        this.price = price;
    }

    public String getProductId() {
        return productId;
    }

    public void setProductId(String productId) {
        this.productId = productId;
    }

    public String getCategoryName() {
        return categoryName;
    }

    public void setCategoryName(String categoryName) {
        this.categoryName = categoryName;
    }

    public String getMainCategoryName() {
        return mainCategoryName;
    }

    public void setMainCategoryName(String mainCategoryName) {
        this.mainCategoryName = mainCategoryName;
    }

    public String getBrand() {
        return brand;
    }

    public void setBrand(String brand) {
        this.brand = brand;
    }

    public BigDecimal getPrice() {
        return price;
    }

    public void setPrice(BigDecimal price) {
        this.price = price;
    }
}
