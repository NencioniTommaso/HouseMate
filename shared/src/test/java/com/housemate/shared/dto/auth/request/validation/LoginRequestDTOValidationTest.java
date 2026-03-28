package com.housemate.shared.dto.auth.request.validation;

import com.housemate.shared.dto.auth.request.LoginRequestDTO;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.tuple;

class LoginRequestDTOValidationTest {

    private static ValidatorFactory validatorFactory;
    private static Validator validator;

    @BeforeAll
    static void setUpValidator() {
        validatorFactory = Validation.buildDefaultValidatorFactory();
        validator = validatorFactory.getValidator();
    }

    @AfterAll
    static void tearDownValidator() {
        validatorFactory.close();
    }

    @Test
    void loginRequest_validDto_hasNoViolations() {
        LoginRequestDTO dto = new LoginRequestDTO("mario.rossi@example.com", "password123");

        Set<ConstraintViolation<LoginRequestDTO>> violations = validator.validate(dto);

        assertThat(violations).isEmpty();
    }

    @Test
    void loginRequest_blankEmail_hasNotBlankViolation() {
        LoginRequestDTO dto = new LoginRequestDTO("", "password123");

        Set<ConstraintViolation<LoginRequestDTO>> violations = validator.validate(dto);

        assertThat(violations)
            .extracting(
                v -> v.getPropertyPath().toString(),
                v -> v.getConstraintDescriptor().getAnnotation().annotationType(),
                v -> v.getInvalidValue()
            )
            .containsExactly(tuple("email", NotBlank.class, ""));
    }

    @Test
    void loginRequest_invalidEmailFormat_hasEmailViolation() {
        LoginRequestDTO dto = new LoginRequestDTO("invalid-email", "password123");

        Set<ConstraintViolation<LoginRequestDTO>> violations = validator.validate(dto);
        
        assertThat(violations)
            .extracting(
                v -> v.getPropertyPath().toString(),
                v -> v.getConstraintDescriptor().getAnnotation().annotationType(),
                v -> v.getInvalidValue()
            )
            .containsExactly(tuple("email", Email.class, "invalid-email"));
    }

    @Test
    void loginRequest_blankPassword_hasNotBlankViolation() {
        LoginRequestDTO dto = new LoginRequestDTO("mario.rossi@example.com", "");

        Set<ConstraintViolation<LoginRequestDTO>> violations = validator.validate(dto);

        assertThat(violations)
            .extracting(
                v -> v.getPropertyPath().toString(),
                v -> v.getConstraintDescriptor().getAnnotation().annotationType(),
                v -> v.getInvalidValue()
            )
            .containsExactly(tuple("password", NotBlank.class, ""));
    }
}