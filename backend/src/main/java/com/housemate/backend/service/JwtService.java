package com.housemate.backend.service;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;
import org.springframework.validation.annotation.Validated;

import com.housemate.backend.service.utils.DateUtils;
import com.housemate.backend.service.utils.JwtUtils;

import javax.crypto.SecretKey;
import java.util.Date;
import java.util.function.Function;

@Service
@Validated
public class JwtService {

    private String secretKey;
    private long expirationMs;

    public JwtService(
        @NotBlank @Value("${jwt.secret}") String secretKey,
        @NotNull @Value("${jwt.expiration-ms}") long expirationMs
    ) {
        this.secretKey = secretKey;
        this.expirationMs = expirationMs;
    }

    public String generateToken(@NotNull UserDetails userDetails) throws IllegalArgumentException {
        if (userDetails.getUsername() == null || userDetails.getUsername().isBlank()) {
            throw new IllegalArgumentException("UserDetails and username cannot be null or blank");
        }

        return Jwts.builder()
                .subject(userDetails.getUsername())
                .issuedAt(new Date(System.currentTimeMillis()))
                .expiration(new Date(System.currentTimeMillis() + expirationMs))
                .signWith(getSigningKey())
                .compact();
    }

    public boolean isTokenValid(@NotBlank String token, @NotNull UserDetails userDetails) {
        try {
            final String tokenSubject = extractSubject(token);
            final Date tokenExpiration = extractExpiration(token);
            if (tokenSubject == null || tokenExpiration == null) {
                return false;
            }
            return tokenSubject.equals(userDetails.getUsername()) && !DateUtils.isDatePassed(tokenExpiration);
        } catch (JwtException e) {
            return false;
        }
    }

    public String extractSubject(@NotBlank String token) throws JwtException {
        return extractClaim(token, Claims::getSubject);
    }

    private Date extractExpiration(@NotBlank String token) throws JwtException {
        return extractClaim(token, Claims::getExpiration);
    }

    private <T> T extractClaim(String token, Function<Claims, T> claimsResolver) throws JwtException {
        return claimsResolver.apply(extractAllClaims(token));
    }

    private Claims extractAllClaims(String token) throws JwtException {
        return Jwts.parser()
                .verifyWith(getSigningKey())
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }

    private SecretKey getSigningKey() {
        return JwtUtils.getSigningKey(secretKey);
    }
}
