package com.graduation.smarty_commerce.io.Entity;

import jakarta.persistence.*;

import java.io.Serial;
import java.util.Collection;

@Entity
@Table(name="cart")
public class CartEntity {

    private static final long serialVersionUID = 1231295114816925194L;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long id;

    private long currentTotal;

    @ManyToMany(cascade = {CascadeType.PERSIST}, fetch = FetchType.LAZY)
    @JoinTable(name="cart-products",
            joinColumns = @JoinColumn(name = "cart_id", referencedColumnName = "id"),
            inverseJoinColumns = @JoinColumn(name = "product_id", referencedColumnName = "id"))
    Collection<ProductEntity> products;

    @OneToOne
    @JoinColumn(name = "user_id", referencedColumnName = "id")
    private UserEntity user;
}
