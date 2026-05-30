package com.graduation.smarty_commerce.io.Repository;
import com.graduation.smarty_commerce.io.Entity.OrderEntity;
import com.graduation.smarty_commerce.shared.OrderStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface OrderRepository extends JpaRepository<OrderEntity, Long> {
    List<OrderEntity> findByUserUserId(String userId);
    OrderEntity findByOrderId(String orderId);
    List<OrderEntity> findByOrderStatus(OrderStatus status);
}
