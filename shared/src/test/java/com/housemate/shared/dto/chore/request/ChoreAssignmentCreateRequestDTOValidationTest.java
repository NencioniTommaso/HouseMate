package com.housemate.shared.dto.chore.request;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.Set;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

@DisplayName("ChoreAssignmentCreateRequestDTO Validation Tests")
class ChoreAssignmentCreateRequestDTOValidationTest {

    private static Validator validator;

    @BeforeAll
    static void setUpValidator() {
        ValidatorFactory factory = Validation.buildDefaultValidatorFactory();
        validator = factory.getValidator();
    }

    @Test
    @DisplayName("Valid DTO: all required fields are present")
    void testValidDTO() {
        ChoreAssignmentCreateRequestDTO dto = new ChoreAssignmentCreateRequestDTO(
                UUID.randomUUID(),
                UUID.randomUUID(),
                null
        );

        Set<ConstraintViolation<ChoreAssignmentCreateRequestDTO>> violations = validator.validate(dto);

        assertTrue(violations.isEmpty(), "No validation errors expected for valid @NotNull annotations");
    }

    @Test
    @DisplayName("@NotNull validation: choreId cannot be null")
    void testNullChoreId() {
        ChoreAssignmentCreateRequestDTO dto = new ChoreAssignmentCreateRequestDTO(
                null,
                UUID.randomUUID(),
                null
        );

        Set<ConstraintViolation<ChoreAssignmentCreateRequestDTO>> violations = validator.validate(dto);

        assertEquals(1, violations.size());
        assertEquals("choreId", violations.iterator().next().getPropertyPath().toString());
    }

    @Test
    @DisplayName("@NotNull validation: assignedUserId cannot be null")
    void testNullAssignedUserId() {
        ChoreAssignmentCreateRequestDTO dto = new ChoreAssignmentCreateRequestDTO(
                UUID.randomUUID(),
                null,
                null
        );

        Set<ConstraintViolation<ChoreAssignmentCreateRequestDTO>> violations = validator.validate(dto);

        assertEquals(1, violations.size());
        assertEquals("assignedUserId", violations.iterator().next().getPropertyPath().toString());
    }
}


