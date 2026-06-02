package com.graduation.smarty_commerce.Service;

import com.graduation.smarty_commerce.ui.Model.Response.RecommendationRest;

/**
 * Service for communicating with the Python recommendation microservice.
 */
public interface RecommendationService {

    /**
     * Get item-to-item recommendations for a specific product.
     * @param productId The product to get recommendations for
     * @param topN Maximum number of recommendations to return
     * @param jwtToken The user's JWT token (forwarded for auth)
     * @return Recommendation response from the Python service
     */
    RecommendationRest getProductRecommendations(String productId, int topN, String jwtToken);

    /**
     * Get personalized recommendations for a user based on their history.
     * @param userId The user to get recommendations for
     * @param topN Maximum number of recommendations to return
     * @param jwtToken The user's JWT token (forwarded for auth)
     * @return Recommendation response from the Python service
     */
    RecommendationRest getUserRecommendations(String userId, int topN, String jwtToken);
}
