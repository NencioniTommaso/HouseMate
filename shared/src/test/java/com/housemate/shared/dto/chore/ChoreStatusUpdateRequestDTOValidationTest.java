package com.housemate.shared.dto.chore;

import com.housemate.shared.dto.chore.request.ChoreStatusUpdateRequestDTO;
import com.housemate.shared.enums.ChoreStatus;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

@DisplayName("ChoreStatusUpdateRequestDTO Validation Tests")
class ChoreStatusUpdateRequestDTOValidationTest {

    private static Validator validator;

    @BeforeAll
    static void setUpValidator() {
        ValidatorFactory factory = Validation.buildDefaultValidatorFactory();
        validator = factory.getValidator();
    }

    @Test
    @DisplayName("Valid DTO: newStatus is present")
    void testValidDTO() {
        ChoreStatusUpdateRequestDTO dto = new ChoreStatusUpdateRequestDTO(
                ChoreStatus.PENDING
        );

        Set<ConstraintViolation<ChoreStatusUpdateRequestDTO>> violations = validator.validate(dto);

        assertTrue(violations.isEmpty(), "No validation errors expected for valid @NotNull newStatus");
    }

    @Test
    @DisplayName("@NotNull validation: newStatus cannot be null")
    void testNullStatus() {
        ChoreStatusUpdateRequestDTO dto = new ChoreStatusUpdateRequestDTO(
                null
        );

        Set<ConstraintViolation<ChoreStatusUpdateRequestDTO>> violations = validator.validate(dto);

        assertEquals(1, violations.size());
        assertEquals("newStatus", violations.iterator().next().getPropertyPath().toString());
    }
}

