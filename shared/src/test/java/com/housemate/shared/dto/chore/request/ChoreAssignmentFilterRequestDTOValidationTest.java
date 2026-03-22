package com.housemate.shared.dto.chore.request;

import com.housemate.shared.enums.ChoreStatus;
import com.housemate.shared.utils.types.DateRange;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Set;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

@DisplayName("ChoreAssignmentFilterRequestDTO Validation Tests")
class ChoreAssignmentFilterRequestDTOValidationTest {

    private static Validator validator;

    @BeforeAll
    static void setUpValidator() {
        ValidatorFactory factory = Validation.buildDefaultValidatorFactory();
        validator = factory.getValidator();
    }

    @Test
    @DisplayName("Valid DTO: dateRange is present")
    void testValidDTO() {
        DateRange dateRange = new DateRange(LocalDateTime.now(), LocalDateTime.now().plusDays(7));
        ChoreAssignmentFilterRequestDTO dto = new ChoreAssignmentFilterRequestDTO(
                null,
                null,
                null,
                dateRange
        );

        Set<ConstraintViolation<ChoreAssignmentFilterRequestDTO>> violations = validator.validate(dto);

        assertTrue(violations.isEmpty(), "No validation errors expected for valid @NotNull dateRange");
    }

    @Test
    @DisplayName("@NotNull validation: dateRange cannot be null")
    void testNullDateRange() {
        ChoreAssignmentFilterRequestDTO dto = new ChoreAssignmentFilterRequestDTO(
                null,
                null,
                null,
                null
        );

        Set<ConstraintViolation<ChoreAssignmentFilterRequestDTO>> violations = validator.validate(dto);

        assertEquals(1, violations.size());
        assertEquals("dateRange", violations.iterator().next().getPropertyPath().toString());
    }
}


