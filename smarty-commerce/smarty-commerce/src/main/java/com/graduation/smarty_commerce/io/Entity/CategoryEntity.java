package com.graduation.smarty_commerce.io.Entity;

import jakarta.persistence.*;

import java.io.Serializable;
import java.util.Collection;

@Entity
@Table(name = "categories")
public class CategoryEntity implements Serializable {

    private static final long serialVersionUID = 5313493413859895455L;


    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long id;

    @Column(nullable = false)
    private String categoryId;

    @Column(nullable = false)
    private String categoryName;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "main_category_id")
    private MainCategoryEntity mainCategory;

    @OneToMany(mappedBy = "category", cascade = CascadeType.ALL, orphanRemoval = true)
    private Collection<ProductEntity> products;

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

    public Collection<ProductEntity> getProducts() {
        return products;
    }

    public void setProducts(Collection<ProductEntity> products) {
        this.products = products;
    }

    public String getCategoryId() {
        return categoryId;
    }

    public void setCategoryId(String categoryId) {
        this.categoryId = categoryId;
    }

    public MainCategoryEntity getMainCategory() {
        return mainCategory;
    }

    public void setMainCategory(MainCategoryEntity mainCategory) {
        this.mainCategory = mainCategory;
    }
}
