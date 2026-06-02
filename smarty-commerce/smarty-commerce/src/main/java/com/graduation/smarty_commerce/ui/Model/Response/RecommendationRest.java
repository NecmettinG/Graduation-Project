package com.graduation.smarty_commerce.ui.Model.Response;

import java.util.List;

/**
 * Response model to deserialize the Python recommendation service's response.
 * Maps to the RecommendationResponse Pydantic model on the Python side.
 */
public class RecommendationRest {

    private String sourceProductId;
    private String sourceUserId;
    private List<RecommendationItem> recommendations;
    private String generatedAt;
    private String algorithm;

    public String getSourceProductId() {
        return sourceProductId;
    }

    public void setSourceProductId(String sourceProductId) {
        this.sourceProductId = sourceProductId;
    }

    public String getSourceUserId() {
        return sourceUserId;
    }

    public void setSourceUserId(String sourceUserId) {
        this.sourceUserId = sourceUserId;
    }

    public List<RecommendationItem> getRecommendations() {
        return recommendations;
    }

    public void setRecommendations(List<RecommendationItem> recommendations) {
        this.recommendations = recommendations;
    }

    public String getGeneratedAt() {
        return generatedAt;
    }

    public void setGeneratedAt(String generatedAt) {
        this.generatedAt = generatedAt;
    }

    public String getAlgorithm() {
        return algorithm;
    }

    public void setAlgorithm(String algorithm) {
        this.algorithm = algorithm;
    }

    /**
     * A single recommendation item with product ID and similarity score.
     */
    public static class RecommendationItem {
        private String productId;
        private double score;

        public String getProductId() {
            return productId;
        }

        public void setProductId(String productId) {
            this.productId = productId;
        }

        public double getScore() {
            return score;
        }

        public void setScore(double score) {
            this.score = score;
        }
    }
}
