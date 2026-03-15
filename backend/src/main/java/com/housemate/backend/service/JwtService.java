package com.housemate.backend.service;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.lang.NonNull;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;
import org.springframework.util.Assert;
import org.springframework.validation.annotation.Validated;

import com.housemate.backend.service.utils.DateUtils;
import com.housemate.backend.service.utils.JwtUtils;

import javax.crypto.SecretKey;
import java.util.Date;
import java.util.Objects;
import java.util.function.Function;

@Service
@Validated
public class JwtService {

    private String secretKey;
    private long expirationMs;

    public JwtService(
        @NonNull @Value("${jwt.secret}") String secretKey,
        @Value("${jwt.expiration-ms}") long expirationMs
    ) {
        Assert.notNull(secretKey, "JWT secret key cannot be null");
        this.secretKey = secretKey;
        this.expirationMs = expirationMs;
    }

    @NonNull
    public String generateToken(@NonNull UserDetails userDetails) throws IllegalArgumentException {
        Assert.notNull(userDetails, "UserDetails cannot be null");

        String username = userDetails.getUsername();
        Assert.notNull(username, "UserDetails username cannot be null");
        Assert.isTrue(!username.isBlank(), "UserDetails username cannot be blank");

        return Objects.requireNonNull(
            Jwts.builder()
                .subject(username)
                .issuedAt(new Date(System.currentTimeMillis()))
                .expiration(new Date(System.currentTimeMillis() + expirationMs))
                .signWith(getSigningKey())
                .compact(),
            "Unexpectedly generated a null JWT token"
        );
    }

    public boolean isTokenValid(@NonNull String token, @NonNull UserDetails userDetails) {
        if (token == null || token.isBlank() || userDetails == null) {
            return false;
        }
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

    public String extractSubject(@NonNull String token) throws JwtException, IllegalArgumentException {
        Assert.notNull(token, "Token cannot be null");
        return extractClaim(token, Claims::getSubject);
    }

    private Date extractExpiration(@NonNull String token) throws JwtException {
        Assert.notNull(token, "Token cannot be null");
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
