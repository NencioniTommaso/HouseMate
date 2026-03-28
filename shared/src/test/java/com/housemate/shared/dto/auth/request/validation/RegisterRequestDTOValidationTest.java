package com.housemate.shared.dto.auth.request.validation;

import com.housemate.shared.dto.auth.request.RegisterRequestDTO;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.tuple;

class RegisterRequestDTOValidationTest {

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
    void registerRequest_validDto_hasNoViolations() {
        RegisterRequestDTO dto = new RegisterRequestDTO(
            "Mario",
            "Rossi",
            "mario.rossi@example.com",
            "password123",
            "IT60X0542811101000000123456"
        );

        Set<ConstraintViolation<RegisterRequestDTO>> violations = validator.validate(dto);

        assertThat(violations).isEmpty();
    }

    @Test
    void registerRequest_noIban_hasNoViolations() {
        RegisterRequestDTO dto = new RegisterRequestDTO(
            "Mario",
            "Rossi",
            "mario.rossi@example.com",
            "password123",
            null
        );

        Set<ConstraintViolation<RegisterRequestDTO>> violations = validator.validate(dto);

        assertThat(violations).isEmpty();
    }

    @Test
    void registerRequest_blankName_hasNotBlankViolation() {
        RegisterRequestDTO dto = new RegisterRequestDTO(
            "",
            "Rossi",
            "mario.rossi@example.com",
            "password123",
            "IT60X0542811101000000123456"
        );

        Set<ConstraintViolation<RegisterRequestDTO>> violations = validator.validate(dto);

        assertThat(violations)
            .extracting(
                v -> v.getPropertyPath().toString(),
                v -> v.getConstraintDescriptor().getAnnotation().annotationType(),
                v -> v.getInvalidValue()
            )
            .containsExactly(tuple("name", NotBlank.class, ""));
    }

    @Test
    void registerRequest_blankSurname_hasNotBlankViolation() {
        RegisterRequestDTO dto = new RegisterRequestDTO(
            "Mario",
            "",
            "mario.rossi@example.com",
            "password123",
            "IT60X0542811101000000123456"
        );

        Set<ConstraintViolation<RegisterRequestDTO>> violations = validator.validate(dto);

        assertThat(violations)
            .extracting(
                v -> v.getPropertyPath().toString(),
                v -> v.getConstraintDescriptor().getAnnotation().annotationType(),
                v -> v.getInvalidValue()
            )
            .containsExactly(tuple("surname", NotBlank.class, ""));
    }

    @Test
    void registerRequest_blankEmail_hasNotBlankViolation() {
        RegisterRequestDTO dto = new RegisterRequestDTO(
            "Mario",
            "Rossi",
            "",
            "password123",
            "IT60X0542811101000000123456"
        );

        Set<ConstraintViolation<RegisterRequestDTO>> violations = validator.validate(dto);

        assertThat(violations)
            .extracting(
                v -> v.getPropertyPath().toString(),
                v -> v.getConstraintDescriptor().getAnnotation().annotationType(),
                v -> v.getInvalidValue()
            )
            .containsExactly(tuple("email", NotBlank.class, ""));
    }

    @Test
    void registerRequest_invalidEmailFormat_hasEmailViolation() {
        RegisterRequestDTO dto = new RegisterRequestDTO(
            "Mario",
            "Rossi",
            "invalid-email",
            "password123",
            "IT60X0542811101000000123456"
        );

        Set<ConstraintViolation<RegisterRequestDTO>> violations = validator.validate(dto);

        assertThat(violations)
            .extracting(
                v -> v.getPropertyPath().toString(),
                v -> v.getConstraintDescriptor().getAnnotation().annotationType(),
                v -> v.getInvalidValue()
            )
            .containsExactly(tuple("email", Email.class, "invalid-email"));
    }

    @Test
    void registerRequest_blankPassword_hasNotBlankViolation() {
        RegisterRequestDTO dto = new RegisterRequestDTO(
            "Mario",
            "Rossi",
            "mario.rossi@example.com",
            "",
            "IT60X0542811101000000123456"
        );

        Set<ConstraintViolation<RegisterRequestDTO>> violations = validator.validate(dto);

        assertThat(violations)
            .extracting(
                v -> v.getPropertyPath().toString(),
                v -> v.getConstraintDescriptor().getAnnotation().annotationType(),
                v -> v.getInvalidValue()
            )
            .containsExactly(tuple("password", NotBlank.class, ""));
    }

    @Test
    void registerRequest_blankIban_hasPatternViolation() {
        RegisterRequestDTO dto = new RegisterRequestDTO(
            "Mario",
            "Rossi",
            "mario.rossi@example.com",
            "password123",
            ""
        );

        Set<ConstraintViolation<RegisterRequestDTO>> violations = validator.validate(dto);

        assertThat(violations)
            .extracting(
                v -> v.getPropertyPath().toString(),
                v -> v.getConstraintDescriptor().getAnnotation().annotationType(),
                v -> v.getInvalidValue()
            )
            .containsExactly(tuple("iban", Pattern.class, ""));
    }

    @Test
    void registerRequest_invalidIban_hasPatternViolation() {
        RegisterRequestDTO dto = new RegisterRequestDTO(
            "Mario",
            "Rossi",
            "mario.rossi@example.com",
            "password123",
            "invalid-iban"
        );

        Set<ConstraintViolation<RegisterRequestDTO>> violations = validator.validate(dto);

        assertThat(violations)
            .extracting(
                v -> v.getPropertyPath().toString(),
                v -> v.getConstraintDescriptor().getAnnotation().annotationType(),
                v -> v.getInvalidValue()

            )
            .containsExactly(tuple("iban", Pattern.class, "invalid-iban"));
    }
}
