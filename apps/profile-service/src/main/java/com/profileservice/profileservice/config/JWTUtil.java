package com.profileservice.profileservice.config;

import com.auth0.jwt.JWT;
import com.auth0.jwt.algorithms.Algorithm;
import com.auth0.jwt.exceptions.JWTVerificationException;
import com.auth0.jwt.interfaces.DecodedJWT;
import com.auth0.jwt.interfaces.JWTVerifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.client.HttpStatusCodeException;
import org.springframework.web.server.ResponseStatusException;

import java.time.Instant;
import java.time.LocalDateTime;
import java.util.Date;


@Component
public class JWTUtil {
    @Value("${jwt.secret}")
    private String secret;
    @Value("${jwt.issuer}")
    private String issuer;

    public String validateTokenAndRetrieveId(String token) throws JWTVerificationException {
        JWTVerifier verifier = JWT.require(Algorithm.HMAC256(secret))
                .withSubject("User Details")
                .withIssuer(issuer)
                .build();
        DecodedJWT jwt = verifier.verify(token);
        if(jwt.getExpiresAt().before(Date.from(Instant.from(LocalDateTime.now())))){
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "JWT was expired");

        }
        return jwt.getClaim("userId").asString();
    }

}
