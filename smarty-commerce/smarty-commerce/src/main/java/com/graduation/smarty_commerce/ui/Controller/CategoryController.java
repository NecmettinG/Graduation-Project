package com.graduation.smarty_commerce.ui.Controller;

import com.graduation.smarty_commerce.Service.CategoryService;
import com.graduation.smarty_commerce.Service.impl.CategoryServiceImpl;
import com.graduation.smarty_commerce.shared.dto.CategoryDto;
import com.graduation.smarty_commerce.shared.dto.MainCategoryDto;
import com.graduation.smarty_commerce.ui.Model.Request.CategoryRequestModel;
import com.graduation.smarty_commerce.ui.Model.Response.CategoryRest;
import com.graduation.smarty_commerce.ui.Model.Response.OperationStatusModel;
import com.graduation.smarty_commerce.ui.Model.Response.RequestOperationName;
import com.graduation.smarty_commerce.ui.Model.Response.RequestOperationStatus;
import org.modelmapper.ModelMapper;
import org.modelmapper.convention.MatchingStrategies;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import org.springframework.security.access.prepost.PreAuthorize;

import java.util.ArrayList;
import java.util.List;

@RestController
@RequestMapping("/categories")
public class CategoryController {

    /*
     modelMapper.getConfiguration().setMatchingStrategy(MatchingStrategies.STRICT):
     By default, ModelMapper tries to map fields intelligently by looking at tokenized property names. Because CategoryRequestModel contains
     mainCategoryId, ModelMapper sees the prefix mainCategory and expects that it should map it to the interior structure of
     MainCategoryDto mainCategory inside CategoryDto. However, the nested mappings become inconsistent and ambiguous.
     Switching the configuration to STRICT forces ModelMapper to demand a perfect token match.
     It will skip mainCategoryId entirely (because CategoryDto doesn't have that primitive field), allowing the explicit instantiation
     and mapping of MainCategoryDto found immediately after in the controller to work without crashing.*/

    @Autowired
    private CategoryServiceImpl categoryService;

    @GetMapping(path = "/{id}")
    public CategoryRest getCategory(@PathVariable String id) {
        CategoryDto categoryDto = categoryService.getCategory(id);
        ModelMapper modelMapper = new ModelMapper();
        return modelMapper.map(categoryDto, CategoryRest.class);
    }

    @PreAuthorize("hasRole('ADMIN')")
    @PostMapping
    public CategoryRest createCategory(@RequestBody CategoryRequestModel categoryDetails) {
        ModelMapper modelMapper = new ModelMapper();
        modelMapper.getConfiguration().setMatchingStrategy(MatchingStrategies.STRICT);
        CategoryDto categoryDto = modelMapper.map(categoryDetails, CategoryDto.class);
        
        if(categoryDetails.getMainCategoryId() != null) {
            MainCategoryDto mainCategoryDto = new MainCategoryDto();
            mainCategoryDto.setCategoryId(categoryDetails.getMainCategoryId());
            categoryDto.setMainCategory(mainCategoryDto);
        }

        CategoryDto createdCategory = categoryService.createCategory(categoryDto);
        return modelMapper.map(createdCategory, CategoryRest.class);
    }

    @PreAuthorize("hasRole('ADMIN')")
    @PutMapping(path = "/{id}")
    public CategoryRest updateCategory(@PathVariable String id, @RequestBody CategoryRequestModel categoryDetails) {
        ModelMapper modelMapper = new ModelMapper();
        modelMapper.getConfiguration().setMatchingStrategy(MatchingStrategies.STRICT);
        CategoryDto categoryDto = modelMapper.map(categoryDetails, CategoryDto.class);
        
        if(categoryDetails.getMainCategoryId() != null) {
            MainCategoryDto mainCategoryDto = new MainCategoryDto();
            mainCategoryDto.setCategoryId(categoryDetails.getMainCategoryId());
            categoryDto.setMainCategory(mainCategoryDto);
        }

        CategoryDto updatedCategory = categoryService.updateCategory(id, categoryDto);
        return modelMapper.map(updatedCategory, CategoryRest.class);
    }

    @PreAuthorize("hasRole('ADMIN')")
    @DeleteMapping(path = "/{id}")
    public OperationStatusModel deleteCategory(@PathVariable String id) {
        OperationStatusModel returnValue = new OperationStatusModel();
        returnValue.setOperationName(RequestOperationName.DELETE.name());

        categoryService.deleteCategory(id);

        returnValue.setOperationResult(RequestOperationStatus.SUCCESS.name());
        return returnValue;
    }

    @GetMapping
    public List<CategoryRest> getCategories(@RequestParam(value = "page", defaultValue = "1") int page,
                                            @RequestParam(value = "limit", defaultValue = "20") int limit) {
        List<CategoryRest> returnValue = new ArrayList<>();

        List<CategoryDto> categories = categoryService.getCategories(page, limit);
        ModelMapper modelMapper = new ModelMapper();

        for (CategoryDto categoryDto : categories) {
            returnValue.add(modelMapper.map(categoryDto, CategoryRest.class));
        }

        return returnValue;
    }
}
