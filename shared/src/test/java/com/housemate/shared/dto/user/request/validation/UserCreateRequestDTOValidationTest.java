package com.housemate.shared.dto.user.request.validation;

import com.housemate.shared.dto.user.request.UserCreateRequestDTO;
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

class UserCreateRequestDTOValidationTest {

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
    void userCreateRequest_validDto_hasNoViolations() {
        UserCreateRequestDTO dto = new UserCreateRequestDTO(
            "Mario",
            "Rossi",
            "mario.rossi@example.com",
            "password123",
            "IT60X0542811101000000123456",
            "https://payments.example.com/mario"
        );

        Set<ConstraintViolation<UserCreateRequestDTO>> violations = validator.validate(dto);

        assertThat(violations).isEmpty();
    }

    @Test
    void userCreateRequest_nullPaymentLink_hasNoViolations() {
        UserCreateRequestDTO dto = new UserCreateRequestDTO(
            "Mario",
            "Rossi",
            "mario.rossi@example.com",
            "password123",
            "IT60X0542811101000000123456",
            null
        );

        Set<ConstraintViolation<UserCreateRequestDTO>> violations = validator.validate(dto);

        assertThat(violations).isEmpty();
    }

    @Test
    void userCreateRequest_blankName_hasNotBlankViolation() {
        UserCreateRequestDTO dto = new UserCreateRequestDTO(
            "",
            "Rossi",
            "mario.rossi@example.com",
            "password123",
            "IT60X0542811101000000123456",
            null
        );

        Set<ConstraintViolation<UserCreateRequestDTO>> violations = validator.validate(dto);

        assertThat(violations)
            .extracting(
                v -> v.getPropertyPath().toString(),
                v -> v.getConstraintDescriptor().getAnnotation().annotationType(),
                ConstraintViolation::getInvalidValue
            )
            .containsExactly(tuple("name", NotBlank.class, ""));
    }

    @Test
    void userCreateRequest_invalidEmailFormat_hasEmailViolation() {
        UserCreateRequestDTO dto = new UserCreateRequestDTO(
            "Mario",
            "Rossi",
            "invalid-email",
            "password123",
            "IT60X0542811101000000123456",
            null
        );

        Set<ConstraintViolation<UserCreateRequestDTO>> violations = validator.validate(dto);

        assertThat(violations)
            .extracting(
                v -> v.getPropertyPath().toString(),
                v -> v.getConstraintDescriptor().getAnnotation().annotationType(),
                ConstraintViolation::getInvalidValue
            )
            .containsExactly(tuple("email", Email.class, "invalid-email"));
    }

    @Test
    void userCreateRequest_blankIban_hasNotBlankAndPatternViolations() {
        UserCreateRequestDTO dto = new UserCreateRequestDTO(
            "Mario",
            "Rossi",
            "mario.rossi@example.com",
            "password123",
            "",
            null
        );

        Set<ConstraintViolation<UserCreateRequestDTO>> violations = validator.validate(dto);

        assertThat(violations)
            .extracting(
                v -> v.getPropertyPath().toString(),
                v -> v.getConstraintDescriptor().getAnnotation().annotationType(),
                ConstraintViolation::getInvalidValue
            )
            .contains(tuple("iban", NotBlank.class, ""))
            .contains(tuple("iban", Pattern.class, ""));
    }

    @Test
    void userCreateRequest_invalidIban_hasPatternViolation() {
        UserCreateRequestDTO dto = new UserCreateRequestDTO(
            "Mario",
            "Rossi",
            "mario.rossi@example.com",
            "password123",
            "invalid-iban",
            null
        );

        Set<ConstraintViolation<UserCreateRequestDTO>> violations = validator.validate(dto);

        assertThat(violations)
            .extracting(
                v -> v.getPropertyPath().toString(),
                v -> v.getConstraintDescriptor().getAnnotation().annotationType(),
                ConstraintViolation::getInvalidValue
            )
            .containsExactly(tuple("iban", Pattern.class, "invalid-iban"));
    }

    @Test
    void userCreateRequest_invalidPaymentLink_hasPatternViolation() {
        UserCreateRequestDTO dto = new UserCreateRequestDTO(
            "Mario",
            "Rossi",
            "mario.rossi@example.com",
            "password123",
            "IT60X0542811101000000123456",
            "ftp://payments.example.com/mario"
        );

        Set<ConstraintViolation<UserCreateRequestDTO>> violations = validator.validate(dto);

        assertThat(violations)
            .extracting(
                v -> v.getPropertyPath().toString(),
                v -> v.getConstraintDescriptor().getAnnotation().annotationType(),
                ConstraintViolation::getInvalidValue
            )
            .containsExactly(tuple("paymentLink", Pattern.class, "ftp://payments.example.com/mario"));
    }
}