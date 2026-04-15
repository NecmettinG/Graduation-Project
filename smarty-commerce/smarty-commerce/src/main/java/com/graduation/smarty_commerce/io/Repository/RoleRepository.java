package com.graduation.smarty_commerce.io.Repository;

import com.graduation.smarty_commerce.io.Entity.RoleEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface RoleRepository extends JpaRepository<RoleEntity, Long> {
}

