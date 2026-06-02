package com.graduation.smarty_commerce.ui.Controller;

import com.graduation.smarty_commerce.Service.DataFeedService;
import com.graduation.smarty_commerce.ui.Model.Response.DataFeedResponse;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtParser;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import com.graduation.smarty_commerce.Security.SecurityConstants;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.crypto.SecretKey;

/**
 * Internal endpoint that exposes aggregated interaction data for the
 * recommendation microservice. Secured by JWT verification — the calling
 * service must present a valid token signed with the same HMAC secret.
 *
 * This endpoint is excluded from Spring Security's filter chain (permitAll)
 * and instead performs manual JWT validation to support service-to-service auth
 * without requiring a UserEntity in the database.
 */
@RestController
@RequestMapping("/internal/data-feed")
public class DataFeedController {

    @Autowired
    private DataFeedService dataFeedService;

    @GetMapping("/interactions")
    public ResponseEntity<?> getInteractions(@RequestHeader(value = "Authorization", required = false) String authHeader) {
        // Validate the JWT from the recommendation service
        if (!isValidServiceToken(authHeader)) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body("{\"error\": \"Invalid or missing service token\"}");
        }

        DataFeedResponse response = dataFeedService.getInteractionData();
        return ResponseEntity.ok(response);
    }

    /**
     * Verify that the Authorization header contains a valid JWT signed with
     * the same secret as this application. The token's subject must be
     * "recommendation-service" to identify it as a service-to-service call.
     */
    private boolean isValidServiceToken(String authHeader) {
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            return false;
        }

        String token = authHeader.replace("Bearer ", "");
        try {
            byte[] secretKeyBytes = SecurityConstants.getTokenSecret().getBytes();
            SecretKey key = Keys.hmacShaKeyFor(secretKeyBytes);

            JwtParser parser = Jwts.parser().verifyWith(key).build();
            Claims claims = parser.parseSignedClaims(token).getPayload();

            String subject = claims.getSubject();
            return "recommendation-service".equals(subject);
        } catch (Exception e) {
            return false;
        }
    }
}
