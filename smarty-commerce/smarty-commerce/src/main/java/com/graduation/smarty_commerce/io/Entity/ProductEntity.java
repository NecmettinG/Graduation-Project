package com.graduation.smarty_commerce.io.Entity;

import jakarta.persistence.*;
import java.util.Collection;

@Entity
@Table(name = "products")
public class ProductEntity {

    private static final long serialVersionUID = 5313493413859873215L;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long id;

    @Column(nullable = false, length = 70)
    private String productName;

    @Column(nullable = false)
    private long price;

    private int stock;

    private String imageUrl;

    private String description;

    //map category with product here, many to many, category will be the owner in joint table.
    @ManyToMany
    private Collection<CategoryEntity> category;

}
