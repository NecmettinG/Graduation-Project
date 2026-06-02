package com.graduation.smarty_commerce.ui.Controller;

import com.graduation.smarty_commerce.Service.ProductService;
import com.graduation.smarty_commerce.Service.RecommendationService;
import com.graduation.smarty_commerce.shared.dto.ProductDto;
import com.graduation.smarty_commerce.ui.Model.Response.ProductRest;
import com.graduation.smarty_commerce.ui.Model.Response.RecommendationRest;
import org.modelmapper.ModelMapper;
import org.modelmapper.convention.MatchingStrategies;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.List;

/**
 * Public-facing recommendation endpoints.
 *
 * Proxies requests to the Python recommendation microservice, then enriches
 * the returned product IDs with full product details (name, price, images, etc.)
 * from the local database. This way the client receives the same ProductRest
 * shape they're already familiar with from other product endpoints.
 */
@RestController
public class RecommendationController {

    @Autowired
    private RecommendationService recommendationService;

    @Autowired
    private ProductService productService;

    /**
     * Get item-to-item recommendations for a specific product.
     * Returns enriched product details for each recommended item.
     */
    @GetMapping("/products/{productId}/recommendations")
    public List<ProductRest> getProductRecommendations(
            @PathVariable String productId,
            @RequestParam(value = "limit", defaultValue = "10") int limit,
            @RequestHeader(value = "Authorization", required = false) String authHeader) {

        RecommendationRest recResponse = recommendationService.getProductRecommendations(productId, limit, authHeader);
        return enrichRecommendations(recResponse);
    }

    /**
     * Get personalized recommendations for a user.
     * Returns enriched product details for each recommended item.
     */
    @GetMapping("/users/{userId}/recommendations")
    public List<ProductRest> getUserRecommendations(
            @PathVariable String userId,
            @RequestParam(value = "limit", defaultValue = "10") int limit,
            @RequestHeader(value = "Authorization", required = false) String authHeader) {

        RecommendationRest recResponse = recommendationService.getUserRecommendations(userId, limit, authHeader);
        return enrichRecommendations(recResponse);
    }

    /**
     * Take the list of recommended product IDs from the Python service
     * and look up full product details for each one.
     */
    private List<ProductRest> enrichRecommendations(RecommendationRest recResponse) {
        List<ProductRest> enrichedProducts = new ArrayList<>();

        if (recResponse == null || recResponse.getRecommendations() == null) {
            return enrichedProducts;
        }

        ModelMapper modelMapper = new ModelMapper();
        modelMapper.getConfiguration().setMatchingStrategy(MatchingStrategies.STRICT);

        for (RecommendationRest.RecommendationItem item : recResponse.getRecommendations()) {
            try {
                ProductDto productDto = productService.getProduct(item.getProductId());
                ProductRest productRest = modelMapper.map(productDto, ProductRest.class);
                enrichedProducts.add(productRest);
            } catch (Exception e) {
                // Skip products that no longer exist or can't be fetched
            }
        }

        return enrichedProducts;
    }
}
