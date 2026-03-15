package com.housemate.backend.service;

import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.Jwts;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.security.core.userdetails.UserDetails;

import com.housemate.backend.service.utils.UserDetailsTestUtils;
import com.housemate.backend.service.utils.JwtUtils;

import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.Date;

import javax.crypto.SecretKey;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class JwtServiceTest {

    private JwtService jwtService;

    private static final String TEST_SECRET = "lVjqXoeWvYOinrSmyU194HEOA9yDJRocWgj6RhnXYMt";
    private static final long EXPIRATION_MS = 3_600_000L; // 1 hour

    @BeforeEach
    void setUp() {
        jwtService = new JwtService(TEST_SECRET, EXPIRATION_MS);
    }

    private String tamperJwtSubject(String originalToken, String newSubject) throws IllegalArgumentException {
        String[] parts = originalToken.split("\\.");
        if (parts.length != 3) {
            throw new IllegalArgumentException("Invalid JWT format");
        }

        String header = parts[0];
        String payload = parts[1];
        String signature = parts[2];

        Base64.Decoder decoder = Base64.getUrlDecoder();
        String decodedPayload = new String(decoder.decode(payload), StandardCharsets.UTF_8);
        if (!decodedPayload.contains("\"sub\":")) {
            throw new IllegalArgumentException("Original token does not contain a subject claim");
        }

        String tamperedPayload;
        tamperedPayload = decodedPayload.replaceAll("\"sub\":\"[^\"]+\"", "\"sub\":\"" + newSubject + "\"");

        Base64.Encoder encoder = Base64.getUrlEncoder().withoutPadding();
        String newPayloadEncoded = encoder.encodeToString(tamperedPayload.getBytes(StandardCharsets.UTF_8));

        return header + "." + newPayloadEncoded + "." + signature;
    }

    // --- generateToken ---

    @Test
    void generateToken_producesNonNullNonBlankToken() {
        String userId = "550e8400-e29b-41d4-a716-446655440000";
        String token = jwtService.generateToken(UserDetailsTestUtils.userDetailsOf(userId));

        assertThat(token).isNotNull().isNotBlank();
    }

    @Test
    void generateToken_producesThreePartJwtStructure() {
        String userId = "550e8400-e29b-41d4-a716-446655440000";
        String token = jwtService.generateToken(UserDetailsTestUtils.userDetailsOf(userId));

        assertThat(token.split("\\.")).hasSize(3);
    }

    @Test
    void generateToken_blankUsername_throwsIllegalArgumentException() {
        UserDetails userDetails = UserDetailsTestUtils.userDetailsOf("   ");

        assertThatThrownBy(() -> jwtService.generateToken(userDetails))
                .isInstanceOf(IllegalArgumentException.class);
    }

    // --- isTokenValid ---

    @Test
    void isTokenValid_matchingUser_returnsTrue() {
        String userId = "550e8400-e29b-41d4-a716-446655440000";
        UserDetails userDetails = UserDetailsTestUtils.userDetailsOf(userId);
        String token = jwtService.generateToken(userDetails);

        assertThat(jwtService.isTokenValid(token, userDetails)).isTrue();
    }

    @Test
    void isTokenValid_differentUserSubject_returnsFalse() {
        String userId = "550e8400-e29b-41d4-a716-446655440000";
        String otherUserId = "660e8400-e29b-41d4-a716-446655440001";
        UserDetails owner = UserDetailsTestUtils.userDetailsOf(userId);
        UserDetails other = UserDetailsTestUtils.userDetailsOf(otherUserId);
        String token = jwtService.generateToken(owner);

        assertThat(jwtService.isTokenValid(token, other)).isFalse();
    }

    @Test
    void isTokenValid_expiredToken_returnsFalse() {
        JwtService shortLivedService = new JwtService(TEST_SECRET, -1000L); // expired 1 second ago

        String userId = "550e8400-e29b-41d4-a716-446655440000";
        UserDetails userDetails = UserDetailsTestUtils.userDetailsOf(userId);
        String token = shortLivedService.generateToken(userDetails);

        assertThat(shortLivedService.isTokenValid(token, userDetails)).isFalse();
    }

    @Test
    void isTokenValid_missingSubjectClaim_returnsFalse() {
        String userId = "550e8400-e29b-41d4-a716-446655440000";
        UserDetails userDetails = UserDetailsTestUtils.userDetailsOf(userId);
        
        SecretKey key = JwtUtils.getSigningKey(TEST_SECRET);
        String tokenNoSub = Jwts.builder()
                .issuedAt(new Date(System.currentTimeMillis()))
                .expiration(new Date(System.currentTimeMillis() + EXPIRATION_MS))
                .signWith(key)
                .compact();

        assertThat(jwtService.isTokenValid(tokenNoSub, userDetails)).isFalse();
    }

    @Test
    void isTokenValid_missingExpirationClaim_returnsFalse() {
        String userId = "550e8400-e29b-41d4-a716-446655440000";
        UserDetails userDetails = UserDetailsTestUtils.userDetailsOf(userId);
        
        SecretKey key = JwtUtils.getSigningKey(TEST_SECRET);
        String tokenNoExp = Jwts.builder()
                .subject(userId)
                .issuedAt(new Date(System.currentTimeMillis()))
                .signWith(key)
                .compact();

        assertThat(jwtService.isTokenValid(tokenNoExp, userDetails)).isFalse();
    }

    @Test
    void isTokenValid_tamperedSignature_returnsFalse() {
        String userId = "550e8400-e29b-41d4-a716-446655440000";
        UserDetails userDetails = UserDetailsTestUtils.userDetailsOf(userId);
        String token = jwtService.generateToken(userDetails);
        String tamperedToken = token.substring(0, token.lastIndexOf('.') + 1) + "TAMPERED_SIGNATURE";

        assertThat(jwtService.isTokenValid(tamperedToken, userDetails)).isFalse();
    }

    @Test
    void isTokenValid_tamperedPayload_returnsFalse() {
        String userId = "550e8400-e29b-41d4-a716-446655440000";
        UserDetails userDetails = UserDetailsTestUtils.userDetailsOf(userId);
        String token = jwtService.generateToken(userDetails);

        String otherUserId = "660e8400-e29b-41d4-a716-446655440001";
        String tamperedToken = tamperJwtSubject(token, otherUserId);

        assertThat(jwtService.isTokenValid(token, userDetails)).isTrue();
        assertThat(jwtService.isTokenValid(tamperedToken, userDetails)).isFalse();
    }

    @Test
    void isTokenValid_completelyInvalidToken_returnsFalse() {
        String userId = "550e8400-e29b-41d4-a716-446655440000";
        UserDetails userDetails = UserDetailsTestUtils.userDetailsOf(userId);

        assertThat(jwtService.isTokenValid("not.a.token", userDetails)).isFalse();
    }

    @Test
    void isTokenValid_signedWithDifferentKey_returnsFalse() {
        String userId = "550e8400-e29b-41d4-a716-446655440000";
        UserDetails userDetails = UserDetailsTestUtils.userDetailsOf(userId);

        String differentSecret = "2EUEz3gL8PGRezst6gg9unUINJP00FByNTDNVrjOKbW";
        JwtService otherService = new JwtService(differentSecret, EXPIRATION_MS);
        
        String foreignToken = otherService.generateToken(userDetails);

        assertThat(jwtService.isTokenValid(foreignToken, userDetails)).isFalse();
    }

    // --- extractSubject ---

    @Test
    void extractSubject_matchesUsername() {
        String userId = "550e8400-e29b-41d4-a716-446655440000";
        UserDetails userDetails = UserDetailsTestUtils.userDetailsOf(userId);

        String token = jwtService.generateToken(userDetails);

        assertThat(jwtService.extractSubject(token)).isEqualTo(userId);
    }

    @Test
    void extractSubject_expiredToken_throwsExpiredJwtException() {
        JwtService shortLivedService = new JwtService(TEST_SECRET, -1000L); // expired 1 second ago

        String userId = "550e8400-e29b-41d4-a716-446655440000";
        UserDetails userDetails = UserDetailsTestUtils.userDetailsOf(userId);
        String token = shortLivedService.generateToken(userDetails);

        assertThatThrownBy(() -> shortLivedService.extractSubject(token))
                .isInstanceOf(ExpiredJwtException.class);
    }

    @Test
    void extractSubject_completelyInvalidToken_throwsJwtException() {
        assertThatThrownBy(() -> jwtService.extractSubject("not.a.token"))
                .isInstanceOf(io.jsonwebtoken.JwtException.class);
    }
}
