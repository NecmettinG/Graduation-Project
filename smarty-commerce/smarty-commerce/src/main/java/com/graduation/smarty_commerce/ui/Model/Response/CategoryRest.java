package com.graduation.smarty_commerce.ui.Model.Response;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

public class CategoryRest {
    private String categoryId;
    private String categoryName;

    @JsonIgnoreProperties("subCategories")
    private MainCategoryRest mainCategory;

    public String getCategoryId() {
        return categoryId;
    }

    public void setCategoryId(String categoryId) {
        this.categoryId = categoryId;
    }

    public String getCategoryName() {
        return categoryName;
    }

    public void setCategoryName(String categoryName) {
        this.categoryName = categoryName;
    }

    public MainCategoryRest getMainCategory() {
        return mainCategory;
    }

    public void setMainCategory(MainCategoryRest mainCategory) {
        this.mainCategory = mainCategory;
    }
}
