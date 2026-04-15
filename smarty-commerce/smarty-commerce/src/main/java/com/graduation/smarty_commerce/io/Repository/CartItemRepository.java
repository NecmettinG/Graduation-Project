package com.graduation.smarty_commerce.io.Repository;

import com.graduation.smarty_commerce.io.Entity.CartItemEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface CartItemRepository extends JpaRepository<CartItemEntity, Long> {
}

