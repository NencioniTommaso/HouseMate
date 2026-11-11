package com.housemate.shared.dto.expense;

import com.housemate.shared.dto.expense.request.ExpenseShareRequestDTO;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.Set;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

@DisplayName("ExpenseShareRequestDTO Validation Tests")
class ExpenseShareRequestDTOValidationTest {

    private static Validator validator;

    @BeforeAll
    static void setUpValidator() {
        ValidatorFactory factory = Validation.buildDefaultValidatorFactory();
        validator = factory.getValidator();
    }

    @Test
    @DisplayName("Valid DTO: userId is present")
    void testValidDTO() {
        ExpenseShareRequestDTO dto = new ExpenseShareRequestDTO(
                UUID.randomUUID(),
                new BigDecimal("20.00")
        );

        Set<ConstraintViolation<ExpenseShareRequestDTO>> violations = validator.validate(dto);

        assertTrue(violations.isEmpty(), "No validation errors expected for valid @NotNull userId");
    }

    @Test
    @DisplayName("@NotNull validation: userId cannot be null")
    void testNullUserId() {
        ExpenseShareRequestDTO dto = new ExpenseShareRequestDTO(
                null,
                new BigDecimal("20.00")
        );

        Set<ConstraintViolation<ExpenseShareRequestDTO>> violations = validator.validate(dto);

        assertEquals(1, violations.size());
        assertEquals("userId", violations.iterator().next().getPropertyPath().toString());
    }
}
