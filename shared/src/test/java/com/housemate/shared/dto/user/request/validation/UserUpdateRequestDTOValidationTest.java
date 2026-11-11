package com.housemate.shared.dto.user.request.validation;

import com.housemate.shared.dto.user.request.UserUpdateRequestDTO;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.tuple;

class UserUpdateRequestDTOValidationTest {

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
    void userUpdateRequest_validDto_hasNoViolations() {
        UserUpdateRequestDTO dto = new UserUpdateRequestDTO(
            "Mario",
            "Rossi",
            "mario.rossi@example.com",
            "IT60X0542811101000000123456",
            "https://payments.example.com/mario"
        );

        Set<ConstraintViolation<UserUpdateRequestDTO>> violations = validator.validate(dto);

        assertThat(violations).isEmpty();
    }

    @Test
    void userUpdateRequest_allNullFields_hasNoViolations() {
        UserUpdateRequestDTO dto = new UserUpdateRequestDTO(null, null, null, null, null);

        Set<ConstraintViolation<UserUpdateRequestDTO>> violations = validator.validate(dto);

        assertThat(violations).isEmpty();
    }

    @Test
    void userUpdateRequest_tooLongName_hasSizeViolation() {
        UserUpdateRequestDTO dto = new UserUpdateRequestDTO(
            "A".repeat(51),
            null,
            null,
            null,
            null
        );

        Set<ConstraintViolation<UserUpdateRequestDTO>> violations = validator.validate(dto);

        assertThat(violations)
            .extracting(
                v -> v.getPropertyPath().toString(),
                v -> v.getConstraintDescriptor().getAnnotation().annotationType(),
                ConstraintViolation::getInvalidValue
            )
            .containsExactly(tuple("name", Size.class, "A".repeat(51)));
    }

    @Test
    void userUpdateRequest_invalidEmailFormat_hasEmailViolation() {
        UserUpdateRequestDTO dto = new UserUpdateRequestDTO(
            null,
            null,
            "invalid-email",
            null,
            null
        );

        Set<ConstraintViolation<UserUpdateRequestDTO>> violations = validator.validate(dto);

        assertThat(violations)
            .extracting(
                v -> v.getPropertyPath().toString(),
                v -> v.getConstraintDescriptor().getAnnotation().annotationType(),
                ConstraintViolation::getInvalidValue
            )
            .containsExactly(tuple("email", Email.class, "invalid-email"));
    }

    @Test
    void userUpdateRequest_invalidIban_hasPatternViolation() {
        UserUpdateRequestDTO dto = new UserUpdateRequestDTO(
            null,
            null,
            null,
            "invalid-iban",
            null
        );

        Set<ConstraintViolation<UserUpdateRequestDTO>> violations = validator.validate(dto);

        assertThat(violations)
            .extracting(
                v -> v.getPropertyPath().toString(),
                v -> v.getConstraintDescriptor().getAnnotation().annotationType(),
                ConstraintViolation::getInvalidValue
            )
            .containsExactly(tuple("iban", Pattern.class, "invalid-iban"));
    }

    @Test
    void userUpdateRequest_invalidPaymentLink_hasPatternViolation() {
        UserUpdateRequestDTO dto = new UserUpdateRequestDTO(
            null,
            null,
            null,
            null,
            "ftp://payments.example.com/mario"
        );

        Set<ConstraintViolation<UserUpdateRequestDTO>> violations = validator.validate(dto);

        assertThat(violations)
            .extracting(
                v -> v.getPropertyPath().toString(),
                v -> v.getConstraintDescriptor().getAnnotation().annotationType(),
                ConstraintViolation::getInvalidValue
            )
            .containsExactly(tuple("paymentLink", Pattern.class, "ftp://payments.example.com/mario"));
    }
}