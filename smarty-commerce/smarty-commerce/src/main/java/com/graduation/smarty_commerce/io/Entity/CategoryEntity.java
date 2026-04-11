package com.graduation.smarty_commerce.io.Entity;

import jakarta.persistence.*;

@Entity
@Table(name = "categories")
public class CategoryEntity {

    private static final long serialVersionUID = 5313493413859895455L;


    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long id;

    @Column(nullable = false)
    private String categoryName;

    @ManyToMany
    private Collection<ProductEntity> products;

}
