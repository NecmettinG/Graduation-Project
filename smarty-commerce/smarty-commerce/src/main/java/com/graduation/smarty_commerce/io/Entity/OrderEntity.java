package com.graduation.smarty_commerce.io.Entity;

import com.graduation.smarty_commerce.shared.OrderStatus;
import jakarta.persistence.*;

import java.util.Collection;
import java.util.Date;
import java.util.List;

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
    private java.math.BigDecimal totalAmount;

    @Column(nullable = false)
    private String shippingAddress;

    @OneToMany(cascade = CascadeType.ALL, mappedBy = "order", orphanRemoval = true)
    private List<OrderItemEntity> orderItems;

    @ManyToOne
    @JoinColumn(name = "user_id")
    private UserEntity user;
}
