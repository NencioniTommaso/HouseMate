package com.housemate.shared.dto.expense;

import com.housemate.shared.dto.expense.request.DebtFilterRequestDTO;
import com.housemate.shared.enums.UserTransactionRole;
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

@DisplayName("DebtFilterRequestDTO Validation Tests")
class DebtFilterRequestDTOValidationTest {

    private static Validator validator;

    @BeforeAll
    static void setUpValidator() {
        ValidatorFactory factory = Validation.buildDefaultValidatorFactory();
        validator = factory.getValidator();
    }

    @Test
    @DisplayName("Valid DTO: userTransactionRole is present")
    void testValidDTO() {
        DebtFilterRequestDTO dto = new DebtFilterRequestDTO(
                UserTransactionRole.DEBTOR,
                UUID.randomUUID()
        );

        Set<ConstraintViolation<DebtFilterRequestDTO>> violations = validator.validate(dto);

        assertTrue(violations.isEmpty(), "No validation errors expected for valid @NotNull userTransactionRole");
    }

    @Test
    @DisplayName("@NotNull validation: userTransactionRole cannot be null")
    void testNullUserTransactionRole() {
        DebtFilterRequestDTO dto = new DebtFilterRequestDTO(
                null,
                UUID.randomUUID()
        );

        Set<ConstraintViolation<DebtFilterRequestDTO>> violations = validator.validate(dto);

        assertEquals(1, violations.size());
        assertEquals("userTransactionRole", violations.iterator().next().getPropertyPath().toString());
    }
}
