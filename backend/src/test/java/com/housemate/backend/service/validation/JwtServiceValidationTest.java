package com.housemate.backend.service.validation;

import jakarta.validation.ConstraintViolationException;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.validation.ValidationAutoConfiguration;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.test.context.ActiveProfiles;

import com.housemate.backend.service.JwtService;
import com.housemate.backend.service.utils.UserDetailsTestUtils;

import static org.assertj.core.api.Assertions.assertThatThrownBy;

@SpringBootTest(classes = {JwtService.class, ValidationAutoConfiguration.class})
@ActiveProfiles("test")
class JwtServiceValidationTest {

    @Autowired
    private JwtService jwtService;

    // --- generateToken ---

    @Test
    void generateToken_nullUserDetails_throwsContraintViolationException() {
        assertThatThrownBy(() -> jwtService.generateToken(null))
            .isInstanceOf(ConstraintViolationException.class);
    }

    // --- validateToken ---

    @Test
    void isTokenValid_nullToken_throwsConstraintViolationException() {
        String userId = "550e8400-e29b-41d4-a716-446655440000";
        UserDetails userDetails = UserDetailsTestUtils.userDetailsOf(userId);

        assertThatThrownBy(() -> jwtService.isTokenValid(null, userDetails))
                .isInstanceOf(ConstraintViolationException.class);
    }

    @Test
    void isTokenValid_blankToken_throwsConstraintViolationException() {
        String userId = "550e8400-e29b-41d4-a716-446655440000";
        UserDetails userDetails = UserDetailsTestUtils.userDetailsOf(userId);

        assertThatThrownBy(() -> jwtService.isTokenValid("", userDetails))
                .isInstanceOf(ConstraintViolationException.class);
        assertThatThrownBy(() -> jwtService.isTokenValid("   ", userDetails))
                .isInstanceOf(ConstraintViolationException.class);
    }

    @Test
    void isTokenValid_nullUserDetails_throwsConstraintViolationException() {
        String userId = "550e8400-e29b-41d4-a716-446655440000";
        String token = jwtService.generateToken(UserDetailsTestUtils.userDetailsOf(userId));

        assertThatThrownBy(() -> jwtService.isTokenValid(token, null))
                .isInstanceOf(ConstraintViolationException.class);
    }

    // --- extractSubject ---

    @Test
    void extractSubject_nullToken_throwsConstraintViolationException() {
        assertThatThrownBy(() -> jwtService.extractSubject(null))
                .isInstanceOf(ConstraintViolationException.class);
    }

    @Test
    void extractSubject_blankToken_throwsConstraintViolationException() {
        assertThatThrownBy(() -> jwtService.extractSubject(""))
                .isInstanceOf(ConstraintViolationException.class);
        assertThatThrownBy(() -> jwtService.extractSubject("   "))
                .isInstanceOf(ConstraintViolationException.class);
    }
}