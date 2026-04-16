package com.graduation.smarty_commerce.shared;

import com.graduation.smarty_commerce.Security.SecurityConstants;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.JwtParser;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.security.SecureRandom;
import java.time.Instant;
import java.util.Date;
import java.util.Random;

@Component
public class Utils {

    private final Random RANDOM = new SecureRandom();
    private final String alphabet = "0123456789ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz";
    private long plusMillisTime = SecurityConstants.EXPIRATION_TIME;

    public String generateId(int length){

        return generateRandomString(length);
    }

    private String generateRandomString(int length){

        StringBuilder returnValue = new StringBuilder(length);

        for(int x = 0; x < length; x++){

            returnValue.append(alphabet.charAt(RANDOM.nextInt(alphabet.length())));
        }

        return new String(returnValue);
    }

    public static boolean hasTokenExpired(String token){


        boolean returnValue = false;

        try {


            byte[] secretKeyBytes = SecurityConstants.getTokenSecret().getBytes();
            SecretKey key = Keys.hmacShaKeyFor(secretKeyBytes);


            JwtParser parser = Jwts.parser().verifyWith(key).build();


            Claims claims = parser.parseSignedClaims(token).getPayload();


            Date tokenExpirationDate = claims.getExpiration();
            Date todayDate = new Date();

            returnValue = tokenExpirationDate.before(todayDate);
        }
        catch (ExpiredJwtException ex) {

            returnValue = true;
        }

        return returnValue;
    }

    public String generateEmailVerificationToken(String userId){

        plusMillisTime = SecurityConstants.EXPIRATION_TIME;

        return generateToken(userId);
    }

    public String generatePasswordResetToken(String userId){

        plusMillisTime = SecurityConstants.PASSWORD_RESET_EXPIRATION_TIME;

        return generateToken(userId);
    }

    private String generateToken(String userId) {

        byte[] secretKeyBytes = SecurityConstants.getTokenSecret().getBytes();
        SecretKey secretKey = Keys.hmacShaKeyFor(secretKeyBytes);
        Instant now = Instant.now();

        return Jwts.builder()
                .subject(userId)
                .expiration(Date.from(now.plusMillis(plusMillisTime)))
                .issuedAt(Date.from(now))
                .signWith(secretKey)
                .compact();
    }
}
