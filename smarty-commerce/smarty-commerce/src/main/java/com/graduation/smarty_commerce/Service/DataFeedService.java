package com.graduation.smarty_commerce.Service;

import com.graduation.smarty_commerce.ui.Model.Response.DataFeedResponse;

/**
 * Service for aggregating user interaction data (orders, wishlists, carts)
 * into a format consumable by the recommendation microservice.
 */
public interface DataFeedService {
    DataFeedResponse getInteractionData();
}
