package com.graduation.smarty_commerce.io.Repository;

import com.graduation.smarty_commerce.io.Entity.MainCategoryEntity;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface MainCategoryRepository extends CrudRepository<MainCategoryEntity, Long> {
    MainCategoryEntity findByCategoryName(String categoryName);
    MainCategoryEntity findByCategoryId(String categoryId);
}

