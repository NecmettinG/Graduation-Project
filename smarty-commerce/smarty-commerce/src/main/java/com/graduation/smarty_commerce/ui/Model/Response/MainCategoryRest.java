package com.graduation.smarty_commerce.ui.Model.Response;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import java.util.List;

public class MainCategoryRest {
    private String categoryId;
    private String categoryName;

    @JsonIgnoreProperties("mainCategory")
    private List<CategoryRest> subCategories;

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

    public List<CategoryRest> getSubCategories() {
        return subCategories;
    }

    public void setSubCategories(List<CategoryRest> subCategories) {
        this.subCategories = subCategories;
    }
}
