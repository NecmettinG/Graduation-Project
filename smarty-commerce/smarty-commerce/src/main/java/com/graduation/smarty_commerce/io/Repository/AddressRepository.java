package com.graduation.smarty_commerce.io.Repository;

import com.graduation.smarty_commerce.io.Entity.AddressEntity;
import com.graduation.smarty_commerce.io.Entity.UserEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface AddressRepository extends JpaRepository<AddressEntity, Long> {

    List<AddressEntity> findAllByUserDetails(UserEntity userEntity);
    AddressEntity findByAddressId(String userId);
}
