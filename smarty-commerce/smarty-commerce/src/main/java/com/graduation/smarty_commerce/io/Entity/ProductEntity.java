package com.graduation.smarty_commerce.io.Entity;

import jakarta.persistence.*;
import java.util.Collection;
import java.util.List;

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
    private java.math.BigDecimal price;

    private int stock;

    private String imageUrl;

    private String description;

    //map category with product here, many to many, category will be the owner in joint table.

    @ManyToMany(mappedBy = "products")
    private Collection<CategoryEntity> category;

    //Cascade type all and orphan removal can be deleted!
    @OneToMany(cascade = CascadeType.ALL, mappedBy = "product", orphanRemoval = true)
    private List<OrderItemEntity> orderItems;

    //Cascade type all and orphan removal can be deleted!
    @OneToMany(cascade = CascadeType.ALL, mappedBy = "product", orphanRemoval = true)
    private List<CartItemEntity> cartItems;
}
