package com.housemate.shared.dto.expense;

import com.housemate.shared.dto.expense.request.SettlementCreateRequestDTO;
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

@DisplayName("SettlementCreateRequestDTO Validation Tests")
class SettlementCreateRequestDTOValidationTest {

    private static Validator validator;

    @BeforeAll
    static void setUpValidator() {
        ValidatorFactory factory = Validation.buildDefaultValidatorFactory();
        validator = factory.getValidator();
    }

    @Test
    @DisplayName("Valid DTO: all required fields are present")
    void testValidDTO() {
        SettlementCreateRequestDTO dto = new SettlementCreateRequestDTO(
                UUID.randomUUID(),
                UUID.randomUUID(),
                new BigDecimal("15.00"),
                "Partial payment"
        );

        Set<ConstraintViolation<SettlementCreateRequestDTO>> violations = validator.validate(dto);

        assertTrue(violations.isEmpty(), "No validation errors expected for a valid DTO");
    }

    @Test
    @DisplayName("@NotNull validation: debtId cannot be null")
    void testNullDebtId() {
        SettlementCreateRequestDTO dto = new SettlementCreateRequestDTO(
                null,
                UUID.randomUUID(),
                new BigDecimal("15.00"),
                "Partial payment"
        );

        Set<ConstraintViolation<SettlementCreateRequestDTO>> violations = validator.validate(dto);

        assertEquals(1, violations.size());
        assertEquals("debtId", violations.iterator().next().getPropertyPath().toString());
    }

    @Test
    @DisplayName("@NotNull validation: creditorId cannot be null")
    void testNullCreditorId() {
        SettlementCreateRequestDTO dto = new SettlementCreateRequestDTO(
                UUID.randomUUID(),
                null,
                new BigDecimal("15.00"),
                "Partial payment"
        );

        Set<ConstraintViolation<SettlementCreateRequestDTO>> violations = validator.validate(dto);

        assertEquals(1, violations.size());
        assertEquals("creditorId", violations.iterator().next().getPropertyPath().toString());
    }

    @Test
    @DisplayName("@NotNull validation: amount cannot be null")
    void testNullAmount() {
        SettlementCreateRequestDTO dto = new SettlementCreateRequestDTO(
                UUID.randomUUID(),
                UUID.randomUUID(),
                null,
                "Partial payment"
        );

        Set<ConstraintViolation<SettlementCreateRequestDTO>> violations = validator.validate(dto);

        assertEquals(1, violations.size());
        assertEquals("amount", violations.iterator().next().getPropertyPath().toString());
    }

    @Test
    @DisplayName("@DecimalMin validation: amount must be strictly positive")
    void testNonPositiveAmount() {
        SettlementCreateRequestDTO dto = new SettlementCreateRequestDTO(
                UUID.randomUUID(),
                UUID.randomUUID(),
                BigDecimal.ZERO,
                "Partial payment"
        );

        Set<ConstraintViolation<SettlementCreateRequestDTO>> violations = validator.validate(dto);

        assertEquals(1, violations.size());
        assertEquals("amount", violations.iterator().next().getPropertyPath().toString());
    }
}
