package com.graduation.smarty_commerce.shared.dto;

import java.io.Serializable;
import java.util.List;

public class MainCategoryDto implements Serializable {

    private static final long serialVersionUID = 5313494389578923648L;

    private long id;
    private String categoryId;
    private String categoryName;
    private List<CategoryDto> subCategories;

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

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

    public List<CategoryDto> getSubCategories() {
        return subCategories;
    }

    public void setSubCategories(List<CategoryDto> subCategories) {
        this.subCategories = subCategories;
    }
}

