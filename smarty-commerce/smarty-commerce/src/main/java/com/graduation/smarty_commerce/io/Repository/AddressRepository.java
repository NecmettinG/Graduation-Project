package com.graduation.smarty_commerce.io.Repository;

import com.graduation.smarty_commerce.io.Entity.AddressEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface AddressRepository extends JpaRepository<AddressEntity, Long> {
}
