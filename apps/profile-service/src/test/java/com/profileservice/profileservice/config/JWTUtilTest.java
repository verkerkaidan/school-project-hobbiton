package com.profileservice.profileservice.config;

import com.auth0.jwt.JWT;
import com.auth0.jwt.algorithms.Algorithm;
import com.auth0.jwt.exceptions.JWTVerificationException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.Date;

import static org.junit.jupiter.api.Assertions.*;

class JWTUtilTest {

    private static final String SECRET = "my-test-secret-key-which-is-long-enough";
    private static final String ISSUER = "test-issuer";
    private static final String USER_ID = "123e4567-e89b-12d3-a456-426614174000";

    private JWTUtil jwtUtil;

    @BeforeEach
    void setUp() {
        jwtUtil = new JWTUtil();
        ReflectionTestUtils.setField(jwtUtil, "secret", SECRET);
        ReflectionTestUtils.setField(jwtUtil, "issuer", ISSUER);
    }

    @Test
    void validateTokenAndRetrieveId_ShouldReturnUserId_WhenTokenIsValid() {
        String token = createValidToken();

        String userId = jwtUtil.validateTokenAndRetrieveId(token);

        assertEquals(USER_ID, userId);
    }

    @Test
    void validateTokenAndRetrieveId_ShouldThrow_WhenTokenIsInvalid() {
        assertThrows(JWTVerificationException.class,
                () -> jwtUtil.validateTokenAndRetrieveId("invalid-token"));
    }

    @Test
    void validateTokenAndRetrieveId_ShouldThrow_WhenTokenIsSignedWithDifferentSecret() {
        String token = JWT.create()
                .withSubject("User Details")
                .withIssuer(ISSUER)
                .withClaim("userId", USER_ID)
                .withIssuedAt(new Date())
                .sign(Algorithm.HMAC256("different-secret-key"));

        assertThrows(JWTVerificationException.class,
                () -> jwtUtil.validateTokenAndRetrieveId(token));
    }

    @Test
    void validateTokenAndRetrieveId_ShouldThrow_WhenTokenHasWrongIssuer() {
        String token = JWT.create()
                .withSubject("User Details")
                .withIssuer("wrong-issuer")
                .withClaim("userId", USER_ID)
                .withIssuedAt(new Date())
                .sign(Algorithm.HMAC256(SECRET));

        assertThrows(JWTVerificationException.class,
                () -> jwtUtil.validateTokenAndRetrieveId(token));
    }

    @Test
    void validateTokenAndRetrieveId_ShouldThrow_WhenTokenIsNull() {
        assertThrows(NullPointerException.class,
                () -> jwtUtil.validateTokenAndRetrieveId(null));
    }

    private String createValidToken() {
        return JWT.create()
                .withSubject("User Details")
                .withIssuer(ISSUER)
                .withClaim("userId", USER_ID)
                .withIssuedAt(new Date())
                .sign(Algorithm.HMAC256(SECRET));
    }
}
