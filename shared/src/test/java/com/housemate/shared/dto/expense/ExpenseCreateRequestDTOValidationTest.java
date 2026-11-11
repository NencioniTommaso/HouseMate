package com.housemate.shared.dto.expense;

import com.housemate.shared.dto.expense.request.ExpenseCreateRequestDTO;
import com.housemate.shared.dto.expense.request.ExpenseShareRequestDTO;
import com.housemate.shared.enums.ExpenseSplitType;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.List;
import java.util.Set;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

@DisplayName("ExpenseCreateRequestDTO Validation Tests")
class ExpenseCreateRequestDTOValidationTest {

    private static Validator validator;

    @BeforeAll
    static void setUpValidator() {
        ValidatorFactory factory = Validation.buildDefaultValidatorFactory();
        validator = factory.getValidator();
    }

    @Test
    @DisplayName("Valid DTO: all required fields are present")
    void testValidDTO() {
        ExpenseCreateRequestDTO dto = new ExpenseCreateRequestDTO(
                "Groceries",
                new BigDecimal("45.00"),
                ExpenseSplitType.EQUAL_SPLIT,
                List.of(new ExpenseShareRequestDTO(UUID.randomUUID(), new BigDecimal("22.50")))
        );

        Set<ConstraintViolation<ExpenseCreateRequestDTO>> violations = validator.validate(dto);

        assertTrue(violations.isEmpty(), "No validation errors expected for a valid DTO");
    }

    @Test
    @DisplayName("@NotBlank validation: description cannot be blank")
    void testBlankDescription() {
        ExpenseCreateRequestDTO dto = new ExpenseCreateRequestDTO(
                "",
                new BigDecimal("45.00"),
                ExpenseSplitType.EQUAL_SPLIT,
                List.of(new ExpenseShareRequestDTO(UUID.randomUUID(), new BigDecimal("22.50")))
        );

        Set<ConstraintViolation<ExpenseCreateRequestDTO>> violations = validator.validate(dto);

        assertEquals(1, violations.size());
        assertEquals("description", violations.iterator().next().getPropertyPath().toString());
    }

    @Test
    @DisplayName("@NotNull validation: amount cannot be null")
    void testNullAmount() {
        ExpenseCreateRequestDTO dto = new ExpenseCreateRequestDTO(
                "Groceries",
                null,
                ExpenseSplitType.EQUAL_SPLIT,
                List.of(new ExpenseShareRequestDTO(UUID.randomUUID(), new BigDecimal("22.50")))
        );

        Set<ConstraintViolation<ExpenseCreateRequestDTO>> violations = validator.validate(dto);

        assertEquals(1, violations.size());
        assertEquals("amount", violations.iterator().next().getPropertyPath().toString());
    }

    @Test
    @DisplayName("@DecimalMin validation: amount must be strictly positive")
    void testNonPositiveAmount() {
        ExpenseCreateRequestDTO dto = new ExpenseCreateRequestDTO(
                "Groceries",
                BigDecimal.ZERO,
                ExpenseSplitType.EQUAL_SPLIT,
                List.of(new ExpenseShareRequestDTO(UUID.randomUUID(), new BigDecimal("22.50")))
        );

        Set<ConstraintViolation<ExpenseCreateRequestDTO>> violations = validator.validate(dto);

        assertEquals(1, violations.size());
        assertEquals("amount", violations.iterator().next().getPropertyPath().toString());
    }

    @Test
    @DisplayName("@NotNull validation: splitType cannot be null")
    void testNullSplitType() {
        ExpenseCreateRequestDTO dto = new ExpenseCreateRequestDTO(
                "Groceries",
                new BigDecimal("45.00"),
                null,
                List.of(new ExpenseShareRequestDTO(UUID.randomUUID(), new BigDecimal("22.50")))
        );

        Set<ConstraintViolation<ExpenseCreateRequestDTO>> violations = validator.validate(dto);

        assertEquals(1, violations.size());
        assertEquals("splitType", violations.iterator().next().getPropertyPath().toString());
    }

    @Test
    @DisplayName("@NotEmpty validation: shares cannot be empty")
    void testEmptyShares() {
        ExpenseCreateRequestDTO dto = new ExpenseCreateRequestDTO(
                "Groceries",
                new BigDecimal("45.00"),
                ExpenseSplitType.EQUAL_SPLIT,
                List.of()
        );

        Set<ConstraintViolation<ExpenseCreateRequestDTO>> violations = validator.validate(dto);

        assertEquals(1, violations.size());
        assertEquals("shares", violations.iterator().next().getPropertyPath().toString());
    }
}
