package com.apigateway.apigateway.config;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.util.List;

@Component
public class JwtUtil {

    private static final Logger logger = LoggerFactory.getLogger(JwtUtil.class);

    private final SecretKey key;
    private final String issuer;

    public JwtUtil(@Value("${jwt.secret}") String secret,
                   @Value("${jwt.issuer}") String issuer) {
        this.key = new SecretKeySpec(
                secret.getBytes(StandardCharsets.UTF_8),
                "HmacSHA256"
        );
        this.issuer = issuer;
    }

    public Claims validateToken(String token) {
        return Jwts.parser()
                .verifyWith(key)
                .requireIssuer(issuer)
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }

    public boolean isAdmin(String token) {
        try {
            Claims claims = validateToken(token);
            List<String> roles = claims.get("role", List.class);
            if (roles == null || !roles.contains("ROLE_ADMIN")) {
                logger.warn("[JwtUtil] Token validated but ROLE_ADMIN not present. roles={}", roles);
                return false;
            }
            return true;
        } catch (ExpiredJwtException e) {
            logger.warn("[JwtUtil] Token rejected: expired");
            return false;
        } catch (JwtException e) {
            logger.warn("[JwtUtil] Token rejected: JWT validation failed — {}", e.getMessage());
            return false;
        }
    }
}
