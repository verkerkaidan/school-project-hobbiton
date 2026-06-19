package com.apigateway.apigateway.config;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.util.List;

@Component
public class JwtUtil {

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
            return roles != null && roles.contains("ROLE_ADMIN");
        } catch (ExpiredJwtException e) {
            return false;
        } catch (JwtException e) {
            return false;
        }
    }
}
