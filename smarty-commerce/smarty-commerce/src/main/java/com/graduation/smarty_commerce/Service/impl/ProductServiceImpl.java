package com.graduation.smarty_commerce.Service.impl;

import com.graduation.smarty_commerce.Exceptions.ProductServiceException;
import com.graduation.smarty_commerce.Service.ProductService;
import com.graduation.smarty_commerce.io.Entity.CategoryEntity;
import com.graduation.smarty_commerce.io.Entity.ProductEntity;
import com.graduation.smarty_commerce.io.Repository.CategoryRepository;
import com.graduation.smarty_commerce.io.Repository.ProductRepository;
import com.graduation.smarty_commerce.shared.Utils;
import com.graduation.smarty_commerce.shared.dto.ProductDto;
import com.graduation.smarty_commerce.ui.Model.Response.ErrorMessages;
import org.modelmapper.ModelMapper;
import org.modelmapper.convention.MatchingStrategies;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
public class ProductServiceImpl implements ProductService {

    @Autowired
    private ProductRepository productRepository;

    @Autowired
    private CategoryRepository categoryRepository;

    @Autowired
    private Utils utils;

    @Override
    public ProductDto createProduct(ProductDto productDto) {
        if (productDto.getCategory() == null || productDto.getCategory().getCategoryId() == null) {
            throw new ProductServiceException("Category ID cannot be null");
        }

        CategoryEntity category = categoryRepository.findByCategoryId(productDto.getCategory().getCategoryId());

        if (category == null) {
            throw new ProductServiceException("Category not found with ID: " + productDto.getCategory().getCategoryId());
        }

        ModelMapper modelMapper = new ModelMapper();
        modelMapper.getConfiguration().setMatchingStrategy(MatchingStrategies.STRICT);
        ProductEntity productEntity = modelMapper.map(productDto, ProductEntity.class);

        productEntity.setProductId(utils.generateId(15));
        productEntity.setCategory(category);

        ProductEntity storedProduct = productRepository.save(productEntity);

        return modelMapper.map(storedProduct, ProductDto.class);
    }

    @Override
    public ProductDto getProduct(String productId) {
        ProductEntity productEntity = productRepository.findByProductId(productId);

        if (productEntity == null) {
            throw new ProductServiceException("Product not found with ID: " + productId);
        }

        ModelMapper modelMapper = new ModelMapper();
        modelMapper.getConfiguration().setMatchingStrategy(MatchingStrategies.STRICT);
        return modelMapper.map(productEntity, ProductDto.class);
    }

    @Override
    public List<ProductDto> getProducts(int page, int limit) {
        List<ProductDto> returnValue = new ArrayList<>();

        if (page > 0) page = page - 1;
        else {
            throw new ProductServiceException(ErrorMessages.INVALID_PAGE_NUMBER.getErrorMessage());
        }

        Pageable pageableRequest = PageRequest.of(page, limit);
        Page<ProductEntity> productsPage = productRepository.findAll(pageableRequest);
        List<ProductEntity> products = productsPage.getContent();

        ModelMapper modelMapper = new ModelMapper();
        modelMapper.getConfiguration().setMatchingStrategy(MatchingStrategies.STRICT);
        for (ProductEntity productEntity : products) {
            returnValue.add(modelMapper.map(productEntity, ProductDto.class));
        }

        return returnValue;
    }

    @Override
    public ProductDto updateProduct(String productId, ProductDto productDto) {
        ProductEntity productEntity = productRepository.findByProductId(productId);

        if (productEntity == null) {
            throw new ProductServiceException("Product not found with ID: " + productId);
        }

        productEntity.setProductName(productDto.getProductName());
        productEntity.setPrice(productDto.getPrice());
        productEntity.setStock(productDto.getStock());
        productEntity.setImageUrls(productDto.getImageUrls());
        productEntity.setDescription(productDto.getDescription());
        productEntity.setAttributes(productDto.getAttributes());

        if (productDto.getCategory() != null && productDto.getCategory().getCategoryId() != null) {
            CategoryEntity category = categoryRepository.findByCategoryId(productDto.getCategory().getCategoryId());
            if (category != null) {
                productEntity.setCategory(category);
            }
        }

        ProductEntity updatedProduct = productRepository.save(productEntity);

        ModelMapper modelMapper = new ModelMapper();
        modelMapper.getConfiguration().setMatchingStrategy(MatchingStrategies.STRICT);
        return modelMapper.map(updatedProduct, ProductDto.class);
    }

    @Override
    public void deleteProduct(String productId) {
        ProductEntity productEntity = productRepository.findByProductId(productId);

        if (productEntity == null) {
            throw new ProductServiceException("Product not found with ID: " + productId);
        }

        productRepository.delete(productEntity);
    }
}
