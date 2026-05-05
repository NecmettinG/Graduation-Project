package com.graduation.smarty_commerce.io.Entity;

import jakarta.persistence.*;

import java.io.Serializable;
import java.util.List;

@Entity
@Table(name = "main_categories")
public class MainCategoryEntity implements Serializable {

    private static final long serialVersionUID = 5313494389578923647L;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long id;

    @Column(nullable = false)
    private String categoryId;

    @Column(nullable = false)
    private String categoryName;

    @OneToMany(cascade = CascadeType.ALL, mappedBy = "mainCategory", orphanRemoval = true)
    private List<CategoryEntity> subCategories;

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

    public List<CategoryEntity> getSubCategories() {
        return subCategories;
    }

    public void setSubCategories(List<CategoryEntity> subCategories) {
        this.subCategories = subCategories;
    }
}
