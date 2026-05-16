package com.graduation.smarty_commerce.ui.Controller;

import com.graduation.smarty_commerce.Service.ProductService;
import com.graduation.smarty_commerce.Service.impl.ProductServiceImpl;
import com.graduation.smarty_commerce.shared.dto.CategoryDto;
import com.graduation.smarty_commerce.shared.dto.ProductDto;
import com.graduation.smarty_commerce.ui.Model.Request.ProductRequestModel;
import com.graduation.smarty_commerce.ui.Model.Response.OperationStatusModel;
import com.graduation.smarty_commerce.ui.Model.Response.ProductRest;
import com.graduation.smarty_commerce.ui.Model.Response.RequestOperationName;
import com.graduation.smarty_commerce.ui.Model.Response.RequestOperationStatus;
import org.modelmapper.ModelMapper;
import org.modelmapper.convention.MatchingStrategies;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.List;

@RestController
@RequestMapping("/products")
public class ProductController {

    @Autowired
    private ProductServiceImpl productService;

    @GetMapping(path = "/{id}")
    public ProductRest getProduct(@PathVariable String id) {
        ProductDto productDto = productService.getProduct(id);
        ModelMapper modelMapper = new ModelMapper();
        modelMapper.getConfiguration().setMatchingStrategy(MatchingStrategies.STRICT);
        return modelMapper.map(productDto, ProductRest.class);
    }

    @PreAuthorize("hasRole('ADMIN')")
    @PostMapping
    public ProductRest createProduct(@RequestBody ProductRequestModel productDetails) {
        ModelMapper modelMapper = new ModelMapper();
        modelMapper.getConfiguration().setMatchingStrategy(MatchingStrategies.STRICT);
        ProductDto productDto = modelMapper.map(productDetails, ProductDto.class);

        if (productDetails.getCategoryId() != null) {
            CategoryDto categoryDto = new CategoryDto();
            categoryDto.setCategoryId(productDetails.getCategoryId());
            productDto.setCategory(categoryDto);
        }

        ProductDto createdProduct = productService.createProduct(productDto);
        return modelMapper.map(createdProduct, ProductRest.class);
    }

    @PreAuthorize("hasRole('ADMIN')")
    @PutMapping(path = "/{id}")
    public ProductRest updateProduct(@PathVariable String id, @RequestBody ProductRequestModel productDetails) {
        ModelMapper modelMapper = new ModelMapper();
        modelMapper.getConfiguration().setMatchingStrategy(MatchingStrategies.STRICT);
        ProductDto productDto = modelMapper.map(productDetails, ProductDto.class);

        if (productDetails.getCategoryId() != null) {
            CategoryDto categoryDto = new CategoryDto();
            categoryDto.setCategoryId(productDetails.getCategoryId());
            productDto.setCategory(categoryDto);
        }

        ProductDto updatedProduct = productService.updateProduct(id, productDto);
        return modelMapper.map(updatedProduct, ProductRest.class);
    }

    @PreAuthorize("hasRole('ADMIN')")
    @DeleteMapping(path = "/{id}")
    public OperationStatusModel deleteProduct(@PathVariable String id) {
        OperationStatusModel returnValue = new OperationStatusModel();
        returnValue.setOperationName(RequestOperationName.DELETE.name());

        productService.deleteProduct(id);

        returnValue.setOperationResult(RequestOperationStatus.SUCCESS.name());
        return returnValue;
    }

    @GetMapping
    public List<ProductRest> getProducts(@RequestParam(value = "page", defaultValue = "1") int page,
                                         @RequestParam(value = "limit", defaultValue = "25") int limit) {
        List<ProductRest> returnValue = new ArrayList<>();

        List<ProductDto> products = productService.getProducts(page, limit);
        ModelMapper modelMapper = new ModelMapper();
        modelMapper.getConfiguration().setMatchingStrategy(MatchingStrategies.STRICT);

        for (ProductDto productDto : products) {
            returnValue.add(modelMapper.map(productDto, ProductRest.class));
        }

        return returnValue;
    }
}
