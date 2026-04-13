package com.graduation.smarty_commerce.io.Entity;

import jakarta.persistence.*;

import java.io.Serial;
import java.util.Collection;
import java.util.List;

@Entity
@Table(name="cart")
public class CartEntity {

    private static final long serialVersionUID = 1231295114816925194L;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long id;

    private java.math.BigDecimal currentTotal;

    @OneToMany(cascade = CascadeType.ALL, mappedBy = "cart", orphanRemoval = true)
    private List<CartItemEntity> cartItems;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", referencedColumnName = "id")
    private UserEntity user;
}
