package com.graduation.smarty_commerce.Service;

import com.graduation.smarty_commerce.ui.Model.Response.DataFeedResponse;
import com.graduation.smarty_commerce.ui.Model.Response.ProductCatalogItem;

import java.util.List;

/**
 * Service for aggregating user interaction data (orders, wishlists, carts)
 * and product catalog metadata into formats consumable by the recommendation microservice.
 */
public interface DataFeedService {
    DataFeedResponse getInteractionData();

    /**
     * Returns a lightweight product catalog containing only the fields needed
     * by the Content-Based Filtering component: category hierarchy, brand, and price.
     */
    List<ProductCatalogItem> getProductCatalog();
}
