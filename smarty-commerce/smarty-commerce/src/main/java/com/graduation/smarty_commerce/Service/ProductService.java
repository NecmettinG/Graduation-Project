package com.graduation.smarty_commerce.Service;

import com.graduation.smarty_commerce.shared.dto.ProductDto;

import java.util.List;
import java.math.BigDecimal;

public interface ProductService {
    ProductDto createProduct(ProductDto productDto);
    ProductDto getProduct(String productId);
    List<ProductDto> getProducts(int page, int limit, String search, BigDecimal minPrice, BigDecimal maxPrice, String categoryId, String sortBy);
    ProductDto updateProduct(String productId, ProductDto productDto);
    void deleteProduct(String productId);
}
