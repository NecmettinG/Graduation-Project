package com.graduation.smarty_commerce.io.Repository;
import com.graduation.smarty_commerce.io.Entity.OrderEntity;
import com.graduation.smarty_commerce.shared.OrderStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface OrderRepository extends JpaRepository<OrderEntity, Long> {
    List<OrderEntity> findByUserUserId(String userId);
    OrderEntity findByOrderId(String orderId);
    List<OrderEntity> findByOrderStatus(OrderStatus status);

    /*
    COMMENT FEATURE SPEED PROBLEM SOLUTION 2:
     Looping memory validation: Whenever a fast hit to /comments was performed, it iteratively fetched and fully serialized-
     the UserEntity's giant historical <OrderEntity> list and all subset order-items directly natively into memory to see-
     if they bought the product!
     Fix: Offloaded that heavy labor entirely directly to PostgreSQL via an ultra-fast structural JPQL COUNT(...) native query implemented-
     inside OrderRepository.
     */
    @Query("SELECT COUNT(o) FROM OrderEntity o JOIN o.orderItems i WHERE o.user.userId = :userId AND i.product.productId = :productId AND o.orderStatus = :status")
    long countUserPurchasesOfProduct(@Param("userId") String userId, @Param("productId") String productId, @Param("status") OrderStatus status);
}
