package com.graduation.smarty_commerce.Service.impl;

import com.graduation.smarty_commerce.Service.RecommendationService;
import com.graduation.smarty_commerce.ui.Model.Response.RecommendationRest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

/**
 * Proxy service that forwards recommendation requests to the Python microservice.
 * Uses RestTemplate to make HTTP calls with the user's JWT token forwarded for auth.
 */
@Service
public class RecommendationServiceImpl implements RecommendationService {

    private static final Logger logger = LoggerFactory.getLogger(RecommendationServiceImpl.class);

    @Value("${recommendation.service.url}")
    private String recommendationServiceUrl;

    private final RestTemplate restTemplate;

    public RecommendationServiceImpl() {
        this.restTemplate = new RestTemplate();
    }

    @Override
    public RecommendationRest getProductRecommendations(String productId, int topN, String jwtToken) {
        String url = recommendationServiceUrl + "/recommendations/product/" + productId + "?top_n=" + topN;
        return callRecommendationService(url, jwtToken);
    }

    @Override
    public RecommendationRest getUserRecommendations(String userId, int topN, String jwtToken) {
        String url = recommendationServiceUrl + "/recommendations/user/" + userId + "?top_n=" + topN;
        return callRecommendationService(url, jwtToken);
    }

    /**
     * Make an HTTP GET request to the recommendation service with the JWT token.
     */
    private RecommendationRest callRecommendationService(String url, String jwtToken) {
        try {
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            if (jwtToken != null) {
                headers.set("Authorization", jwtToken);
            }

            HttpEntity<Void> entity = new HttpEntity<>(headers);

            ResponseEntity<RecommendationRest> response = restTemplate.exchange(
                    url,
                    HttpMethod.GET,
                    entity,
                    RecommendationRest.class
            );

            return response.getBody();
        } catch (RestClientException e) {
            logger.error("Failed to call recommendation service at {}: {}", url, e.getMessage());
            // Return an empty result rather than failing the entire request
            RecommendationRest emptyResult = new RecommendationRest();
            emptyResult.setRecommendations(java.util.Collections.emptyList());
            emptyResult.setAlgorithm("item-item-cf-cosine");
            return emptyResult;
        }
    }
}
