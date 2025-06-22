package com.uef.library.service;

import com.auth0.jwt.JWT;
import com.auth0.jwt.algorithms.Algorithm;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;

@Service
public class StringeeAuthService {

    @Value("${stringee.api.key.sid}")
    private String stringeeApiKeySid;

    @Value("${stringee.api.key.secret}")
    private String stringeeApiKeySecret;

    private static final long EXPIRATION_TIME_SECONDS = 3600;

    public String generateAccessToken() {
        try {
            Algorithm algorithmHS = Algorithm.HMAC256(stringeeApiKeySecret);

            Map<String, Object> headerClaims = new HashMap<>();
            headerClaims.put("typ", "JWT");
            headerClaims.put("alg", "HS256");
            headerClaims.put("cty", "stringee-api;v=1");

            long exp = System.currentTimeMillis() + EXPIRATION_TIME_SECONDS * 1000;

            String accessToken = JWT.create().withHeader(headerClaims)
                    .withClaim("jti", stringeeApiKeySid + "-" + System.currentTimeMillis())
                    .withClaim("iss", stringeeApiKeySid)
                    .withClaim("rest_api", true)
                    .withExpiresAt(new Date(exp))
                    .sign(algorithmHS);

            System.out.println("Stringee Access Token Generated.");
            return accessToken;
        } catch (Exception ex) {
            System.err.println("Error generating Stringee Access Token: " + ex.getMessage());
            return null;
        }
    }
}