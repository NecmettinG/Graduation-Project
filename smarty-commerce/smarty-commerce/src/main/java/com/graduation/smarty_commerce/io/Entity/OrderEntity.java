package com.graduation.smarty_commerce.io.Entity;

import com.graduation.smarty_commerce.shared.OrderStatus;
import jakarta.persistence.*;

import java.util.Collection;
import java.util.Date;

@Entity
@Table(name="orders_table") //You have to be careful when you create a table from order entity because jpa is confusing!
public class OrderEntity {

    private static final long serialVersionUID = 5313493413859832168L;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long id;

    @Column(nullable = false)
    private Date orderDate;

    @Column(nullable = false)
    private String userId;

    @Column(nullable = false)
    private OrderStatus orderStatus;

    @Column(nullable = false)
    private long totalAmount;

    @Column(nullable = false)
    private String shippingAddress;

    @ManyToMany(cascade = {CascadeType.PERSIST}, fetch = FetchType.EAGER)
    @JoinTable(name="order_products",
            joinColumns = @JoinColumn(name="order_id", referencedColumnName = "id"),
            inverseJoinColumns = @JoinColumn(name="product_id", referencedColumnName = "id"))
    private Collection<ProductEntity> products;

    @ManyToOne
    @JoinColumn(name = "user_id")
    private UserEntity user;
}
