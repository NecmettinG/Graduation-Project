package com.graduation.smarty_commerce.Service;

import com.graduation.smarty_commerce.shared.dto.CategoryDto;
import java.util.List;

public interface CategoryService {
    CategoryDto createCategory(CategoryDto categoryDto);
    CategoryDto getCategory(String categoryId);
    List<CategoryDto> getCategories(int page, int limit);
    CategoryDto updateCategory(String categoryId, CategoryDto categoryDto);
    void deleteCategory(String categoryId);
}
