package com.graduation.smarty_commerce.Service.impl;

import com.graduation.smarty_commerce.Service.DataFeedService;
import com.graduation.smarty_commerce.io.Entity.*;
import com.graduation.smarty_commerce.io.Repository.OrderRepository;
import com.graduation.smarty_commerce.io.Repository.ProductRepository;
import com.graduation.smarty_commerce.io.Repository.UserRepository;
import com.graduation.smarty_commerce.ui.Model.Response.DataFeedResponse;
import com.graduation.smarty_commerce.ui.Model.Response.ProductCatalogItem;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Aggregates interaction data from orders, wishlists, and carts
 * into a unified DataFeedResponse for the recommendation service.
 *
 * Also provides a lightweight product catalog for Content-Based Filtering.
 */
@Service
public class DataFeedServiceImpl implements DataFeedService {

    @Autowired
    private OrderRepository orderRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private ProductRepository productRepository;

    @Override
    public DataFeedResponse getInteractionData() {
        List<DataFeedResponse.UserInteraction> orderInteractions = buildOrderInteractions();
        List<DataFeedResponse.UserInteraction> wishlistInteractions = buildWishlistInteractions();
        List<DataFeedResponse.UserInteraction> cartInteractions = buildCartInteractions();

        return new DataFeedResponse(orderInteractions, wishlistInteractions, cartInteractions);
    }

    @Override
    public List<ProductCatalogItem> getProductCatalog() {
        List<ProductEntity> allProducts = productRepository.findAll();
        List<ProductCatalogItem> catalog = new ArrayList<>();

        for (ProductEntity product : allProducts) {
            String categoryName = "";
            String mainCategoryName = "";

            CategoryEntity category = product.getCategory();
            if (category != null) {
                categoryName = category.getCategoryName();
                MainCategoryEntity mainCategory = category.getMainCategory();
                if (mainCategory != null) {
                    mainCategoryName = mainCategory.getCategoryName();
                }
            }

            // Extract brand from the attributes JSON map
            String brand = "";
            Map<String, Object> attributes = product.getAttributes();
            if (attributes != null && attributes.containsKey("brand")) {
                brand = String.valueOf(attributes.get("brand"));
            }

            catalog.add(new ProductCatalogItem(
                    product.getProductId(),
                    categoryName,
                    mainCategoryName,
                    brand,
                    product.getPrice()
            ));
        }

        return catalog;
    }

    /**
     * Query all orders and group the purchased product IDs by user.
     * Each order may contain multiple OrderItemEntities, each referencing a product.
     */
    private List<DataFeedResponse.UserInteraction> buildOrderInteractions() {
        List<OrderEntity> allOrders = orderRepository.findAll();
        Map<String, List<String>> userProductMap = new HashMap<>();

        for (OrderEntity order : allOrders) {
            String userId = order.getUser().getUserId();
            List<OrderItemEntity> items = order.getOrderItems();
            if (items != null) {
                for (OrderItemEntity item : items) {
                    userProductMap
                            .computeIfAbsent(userId, k -> new ArrayList<>())
                            .add(item.getProduct().getProductId());
                }
            }
        }

        return userProductMap.entrySet().stream()
                .map(entry -> new DataFeedResponse.UserInteraction(entry.getKey(), entry.getValue()))
                .collect(Collectors.toList());
    }

    /**
     * Query all users and extract their wishlist product IDs.
     */
    private List<DataFeedResponse.UserInteraction> buildWishlistInteractions() {
        List<UserEntity> allUsers = userRepository.findAll();
        List<DataFeedResponse.UserInteraction> interactions = new ArrayList<>();

        for (UserEntity user : allUsers) {
            List<ProductEntity> wishlist = user.getWishlist();
            if (wishlist != null && !wishlist.isEmpty()) {
                List<String> productIds = wishlist.stream()
                        .map(ProductEntity::getProductId)
                        .collect(Collectors.toList());
                interactions.add(new DataFeedResponse.UserInteraction(user.getUserId(), productIds));
            }
        }

        return interactions;
    }

    /**
     * Query all users and extract the product IDs currently in their carts.
     */
    private List<DataFeedResponse.UserInteraction> buildCartInteractions() {
        List<UserEntity> allUsers = userRepository.findAll();
        List<DataFeedResponse.UserInteraction> interactions = new ArrayList<>();

        for (UserEntity user : allUsers) {
            CartEntity cart = user.getCart();
            if (cart != null && cart.getCartItems() != null && !cart.getCartItems().isEmpty()) {
                List<String> productIds = cart.getCartItems().stream()
                        .map(cartItem -> cartItem.getProduct().getProductId())
                        .collect(Collectors.toList());
                interactions.add(new DataFeedResponse.UserInteraction(user.getUserId(), productIds));
            }
        }

        return interactions;
    }
}
