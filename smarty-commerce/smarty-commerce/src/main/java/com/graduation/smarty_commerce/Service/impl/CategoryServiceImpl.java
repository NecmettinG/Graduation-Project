package com.graduation.smarty_commerce.Service.impl;

import com.graduation.smarty_commerce.Exceptions.CategoryServiceException;
import com.graduation.smarty_commerce.Service.CategoryService;
import com.graduation.smarty_commerce.io.Entity.CategoryEntity;
import com.graduation.smarty_commerce.io.Entity.MainCategoryEntity;
import com.graduation.smarty_commerce.io.Repository.CategoryRepository;
import com.graduation.smarty_commerce.io.Repository.MainCategoryRepository;
import com.graduation.smarty_commerce.shared.Utils;
import com.graduation.smarty_commerce.shared.dto.CategoryDto;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
public class CategoryServiceImpl implements CategoryService {

    @Autowired
    private CategoryRepository categoryRepository;

    @Autowired
    private MainCategoryRepository mainCategoryRepository;

    @Autowired
    private Utils utils;

    @Override
    public CategoryDto createCategory(CategoryDto categoryDto) {
        MainCategoryEntity mainCategory = mainCategoryRepository.findByCategoryId(categoryDto.getMainCategory().getCategoryId());

        if (mainCategory == null) {
            throw new CategoryServiceException("Main category not found");
        }

        if (categoryRepository.findByCategoryNameAndMainCategory(categoryDto.getCategoryName(), mainCategory) != null) {
            throw new CategoryServiceException("Record already exists");
        }

        ModelMapper modelMapper = new ModelMapper();
        CategoryEntity categoryEntity = modelMapper.map(categoryDto, CategoryEntity.class);

        categoryEntity.setCategoryId(utils.generateId(10));
        categoryEntity.setMainCategory(mainCategory);

        CategoryEntity storedCategory = categoryRepository.save(categoryEntity);

        return modelMapper.map(storedCategory, CategoryDto.class);
    }

    @Override
    public CategoryDto getCategory(String categoryId) {
        CategoryEntity categoryEntity = categoryRepository.findByCategoryId(categoryId);

        if (categoryEntity == null) {
            throw new CategoryServiceException("Category not found with ID: " + categoryId);
        }

        ModelMapper modelMapper = new ModelMapper();
        return modelMapper.map(categoryEntity, CategoryDto.class);
    }

    @Override
    public List<CategoryDto> getCategories(int page, int limit) {
        List<CategoryDto> returnValue = new ArrayList<>();

        if (page > 0) page = page - 1;

        Pageable pageableRequest = PageRequest.of(page, limit);
        Page<CategoryEntity> categoriesPage = categoryRepository.findAll(pageableRequest);
        List<CategoryEntity> categories = categoriesPage.getContent();

        ModelMapper modelMapper = new ModelMapper();
        for (CategoryEntity categoryEntity : categories) {
            returnValue.add(modelMapper.map(categoryEntity, CategoryDto.class));
        }

        return returnValue;
    }

    @Override
    public CategoryDto updateCategory(String categoryId, CategoryDto categoryDto) {
        CategoryEntity categoryEntity = categoryRepository.findByCategoryId(categoryId);

        if (categoryEntity == null) {
            throw new CategoryServiceException("Category not found with ID: " + categoryId);
        }

        categoryEntity.setCategoryName(categoryDto.getCategoryName());

        if (categoryDto.getMainCategory() != null && categoryDto.getMainCategory().getCategoryId() != null) {
            MainCategoryEntity mainCategory = mainCategoryRepository.findByCategoryId(categoryDto.getMainCategory().getCategoryId());
            if (mainCategory != null) {
                categoryEntity.setMainCategory(mainCategory);
            }
        }

        CategoryEntity updatedCategory = categoryRepository.save(categoryEntity);

        ModelMapper modelMapper = new ModelMapper();
        return modelMapper.map(updatedCategory, CategoryDto.class);
    }

    @Override
    public void deleteCategory(String categoryId) {
        CategoryEntity categoryEntity = categoryRepository.findByCategoryId(categoryId);

        if (categoryEntity == null) {
            throw new CategoryServiceException("Category not found with ID: " + categoryId);
        }

        categoryRepository.delete(categoryEntity);
    }
}
