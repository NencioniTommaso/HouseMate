package com.housemate.backend.service.utils;

import javax.crypto.SecretKey;

import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;

public class JwtUtils {
    public static SecretKey getSigningKey(String secretKey) throws IllegalArgumentException {
        if (secretKey == null || secretKey.isBlank()) {
            throw new IllegalArgumentException("Secret key cannot be null or blank");
        }
        return Keys.hmacShaKeyFor(Decoders.BASE64.decode(secretKey));
    }
}
