package com.graduation.smarty_commerce.io.Entity;

import jakarta.persistence.*;

import java.util.Collection;

@Entity
@Table(name = "categories")
public class CategoryEntity {

    private static final long serialVersionUID = 5313493413859895455L;


    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long id;

    @Column(nullable = false)
    private String categoryName;

    @ManyToMany(cascade = {CascadeType.PERSIST}, fetch = FetchType.LAZY)
    @JoinTable(name="category_products",
            joinColumns = @JoinColumn(name="category_id", referencedColumnName = "id"),
            inverseJoinColumns = @JoinColumn(name="product_id", referencedColumnName = "id")
    )
    private Collection<ProductEntity> products;

}
