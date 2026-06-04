package com.authenticationservice.authenticationService;

import com.auth0.jwt.JWT;
import com.auth0.jwt.algorithms.Algorithm;
import com.auth0.jwt.exceptions.JWTVerificationException;
import com.auth0.jwt.interfaces.DecodedJWT;
import com.authenticationservice.authenticationservice.config.JWTUtil;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.Base64;
import java.util.Date;
import java.util.List;
import java.util.Vector;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
class JWTUtilTest {

    @Autowired
    private JWTUtil jwtUtil;

    // ---- generateToken ----

    @Test
    void shouldGenerateNonNullToken() {
        String token = jwtUtil.generateToken("user-123", new Vector<>(List.of("ROLE_USER")));
        assertNotNull(token);
    }

    @Test
    void shouldGenerateTokenWithThreeParts() {
        String token = jwtUtil.generateToken("user-123", new Vector<>(List.of("ROLE_USER")));
        // JWT format is header.payload.signature
        assertEquals(3, token.split("\\.").length);
    }

    @Test
    void shouldEmbedUserIdInToken() {
        String token = jwtUtil.generateToken("user-123", new Vector<>(List.of("ROLE_USER")));
        String userId = String.valueOf(jwtUtil.validateTokenAndRetrieveSubject(token));
        assertEquals("user-123", userId);
    }

    @Test
    void shouldSetExpirationApproximatelySixHoursFromNow() {
        long beforeMs = System.currentTimeMillis();
        String token = jwtUtil.generateToken("user-123", new Vector<>(List.of("ROLE_USER")));

        DecodedJWT decoded = JWT.decode(token);
        long expiresAt = decoded.getExpiresAt().getTime();

        long sixHoursMs = 6 * 60 * 60 * 1000L;
        long afterMs = System.currentTimeMillis();

        // Expiry should be between (now + 6h) with a few seconds of tolerance
        assertTrue(expiresAt >= beforeMs + sixHoursMs - 5000);
        assertTrue(expiresAt <= afterMs  + sixHoursMs + 5000);
    }

    @Test
    void shouldSetIssuedAtToNow() {
        long beforeMs = System.currentTimeMillis();
        String token = jwtUtil.generateToken("user-123", new Vector<>(List.of("ROLE_USER")));
        long afterMs = System.currentTimeMillis();

        DecodedJWT decoded = JWT.decode(token);
        long issuedAt = decoded.getIssuedAt().getTime();

        assertTrue(issuedAt >= beforeMs - 1000);
        assertTrue(issuedAt <= afterMs  + 1000);
    }

    // ---- validateTokenAndRetrieveSubject ----

    @Test
    void shouldReturnUserIdFromValidToken() {
        String token = jwtUtil.generateToken("user-456", new Vector<>(List.of("ROLE_USER")));
        String result = String.valueOf(jwtUtil.validateTokenAndRetrieveSubject(token));
        assertEquals("user-456", result);
    }

    @Test
    void shouldThrowOnTamperedSignature() {
        String token = jwtUtil.generateToken("user-123", new Vector<>(List.of("ROLE_USER")));
        String tampered = token.substring(0, token.lastIndexOf('.') + 1) + "badsignature";

        assertThrows(JWTVerificationException.class,
                () -> jwtUtil.validateTokenAndRetrieveSubject(tampered));
    }

    @Test
    void shouldThrowOnTamperedPayload() {
        String token = jwtUtil.generateToken("user-123", new Vector<>(List.of("ROLE_USER")));
        String[] parts = token.split("\\.");

        // Replace payload with a modified one
        String fakePayload = Base64.getUrlEncoder().withoutPadding()
                .encodeToString("{\"userId\":\"hacker\"}".getBytes());
        String tampered = parts[0] + "." + fakePayload + "." + parts[2];

        assertThrows(JWTVerificationException.class,
                () -> jwtUtil.validateTokenAndRetrieveSubject(tampered));
    }

    @Test
    void shouldThrowOnEmptyToken() {
        assertThrows(JWTVerificationException.class,
                () -> jwtUtil.validateTokenAndRetrieveSubject(""));
    }

    @Test
    void shouldThrowOnMalformedToken() {
        assertThrows(JWTVerificationException.class,
                () -> jwtUtil.validateTokenAndRetrieveSubject("not.a.token"));
    }

    @Test
    void shouldThrowOnExpiredToken() {
        // Build an already-expired token directly using auth0
        String secret = "your-secret"; // inject or mock
        String expiredToken = JWT.create()
                .withSubject("User Details")
                .withClaim("userId", "user-123")
                .withIssuedAt(new Date(System.currentTimeMillis() - 10000))
                .withExpiresAt(new Date(System.currentTimeMillis() - 5000)) // expired 5s ago
                .withIssuer("your-issuer")
                .sign(Algorithm.HMAC256(secret));

        assertThrows(JWTVerificationException.class,
                () -> jwtUtil.validateTokenAndRetrieveSubject(expiredToken));
    }

    // ---- edge cases ----

    @Test
    void differentUserIdsShouldProduceDifferentTokens() {
        String token1 = jwtUtil.generateToken("user-1", new Vector<>(List.of("ROLE_USER")));
        String token2 = jwtUtil.generateToken("user-2", new Vector<>(List.of("ROLE_USER")));
        assertNotEquals(token1, token2);
    }

    @Test
    void sameUserIdShouldProduceDifferentTokensOverTime() throws Exception {
        String token1 = jwtUtil.generateToken("user-1", new Vector<>(List.of("ROLE_USER")));
        Thread.sleep(1100); // issuedAt has 1s resolution
        String token2 = jwtUtil.generateToken("user-2", new Vector<>(List.of("ROLE_USER")));
        assertNotEquals(token1, token2);
    }
}