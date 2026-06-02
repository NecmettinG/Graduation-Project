package com.graduation.smarty_commerce.ui.Model.Response;

import java.util.List;

/**
 * Response model for the internal data-feed endpoint.
 * Aggregates user interaction data (orders, wishlists, carts) for the
 * recommendation service to build its item-item similarity matrix.
 */
public class DataFeedResponse {

    private List<UserInteraction> orders;
    private List<UserInteraction> wishlists;
    private List<UserInteraction> carts;

    public DataFeedResponse() {
    }

    public DataFeedResponse(List<UserInteraction> orders, List<UserInteraction> wishlists, List<UserInteraction> carts) {
        this.orders = orders;
        this.wishlists = wishlists;
        this.carts = carts;
    }

    public List<UserInteraction> getOrders() {
        return orders;
    }

    public void setOrders(List<UserInteraction> orders) {
        this.orders = orders;
    }

    public List<UserInteraction> getWishlists() {
        return wishlists;
    }

    public void setWishlists(List<UserInteraction> wishlists) {
        this.wishlists = wishlists;
    }

    public List<UserInteraction> getCarts() {
        return carts;
    }

    public void setCarts(List<UserInteraction> carts) {
        this.carts = carts;
    }

    /**
     * Represents a single user's interaction with a set of products.
     * Used uniformly for orders, wishlists, and carts.
     */
    public static class UserInteraction {
        private String userId;
        private List<String> productIds;

        public UserInteraction() {
        }

        public UserInteraction(String userId, List<String> productIds) {
            this.userId = userId;
            this.productIds = productIds;
        }

        public String getUserId() {
            return userId;
        }

        public void setUserId(String userId) {
            this.userId = userId;
        }

        public List<String> getProductIds() {
            return productIds;
        }

        public void setProductIds(List<String> productIds) {
            this.productIds = productIds;
        }
    }
}
