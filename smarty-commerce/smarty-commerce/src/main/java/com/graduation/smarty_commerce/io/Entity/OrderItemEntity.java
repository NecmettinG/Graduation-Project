package com.graduation.smarty_commerce.io.Entity;

import jakarta.persistence.*;

@Entity
@Table(name = "order_item")
public class OrderItemEntity {

    private static final long serialVersionUID = 1231295792657898289L;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long id;

    @ManyToOne
    @JoinColumn(name = "product_id")
    private ProductEntity product;

    @ManyToOne
    @JoinColumn(name = "order_id")
    private OrderEntity order;

    @Column(nullable = false)
    private int quantity;

    @Column(nullable = false)
    private java.math.BigDecimal unitPrice;
}
