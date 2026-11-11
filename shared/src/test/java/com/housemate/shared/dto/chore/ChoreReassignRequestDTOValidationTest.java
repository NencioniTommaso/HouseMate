package com.housemate.shared.dto.chore;

import com.housemate.shared.dto.chore.request.ChoreReassignRequestDTO;
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

@DisplayName("ChoreReassignRequestDTO Validation Tests")
class ChoreReassignRequestDTOValidationTest {

    private static Validator validator;

    @BeforeAll
    static void setUpValidator() {
        ValidatorFactory factory = Validation.buildDefaultValidatorFactory();
        validator = factory.getValidator();
    }

    @Test
    @DisplayName("Valid DTO: newAssigneeId is present")
    void testValidDTO() {
        ChoreReassignRequestDTO dto = new ChoreReassignRequestDTO(
                UUID.randomUUID()
        );

        Set<ConstraintViolation<ChoreReassignRequestDTO>> violations = validator.validate(dto);

        assertTrue(violations.isEmpty(), "No validation errors expected for valid @NotNull newAssigneeId");
    }

    @Test
    @DisplayName("@NotNull validation: newAssigneeId cannot be null")
    void testNullNewAssigneeId() {
        ChoreReassignRequestDTO dto = new ChoreReassignRequestDTO(
                null
        );

        Set<ConstraintViolation<ChoreReassignRequestDTO>> violations = validator.validate(dto);

        assertEquals(1, violations.size());
        assertEquals("newAssigneeId", violations.iterator().next().getPropertyPath().toString());
    }
}

