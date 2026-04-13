package com.graduation.smarty_commerce.io.Entity;

import jakarta.persistence.*;

@Entity
@Table(name="cart_item")
public class CartItemEntity {

    private static final long serialVersionUID = 1231295114816987231L;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long id;

    @ManyToOne
    @JoinColumn(name = "cart_id")
    private CartEntity cart;

    @ManyToOne
    @JoinColumn(name = "product_id")
    private ProductEntity product;

    @Column(nullable = false)
    private int quantity;

}
