package com.housemate.shared.dto.expense;

import com.housemate.shared.dto.expense.request.TransactionFilterRequestDTO;
import com.housemate.shared.enums.UserTransactionRole;
import com.housemate.shared.utils.types.DateRange;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.util.Set;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertTrue;

@DisplayName("TransactionFilterRequestDTO Validation Tests")
class TransactionFilterRequestDTOValidationTest {

    private static Validator validator;

    @BeforeAll
    static void setUpValidator() {
        ValidatorFactory factory = Validation.buildDefaultValidatorFactory();
        validator = factory.getValidator();
    }

    @Test
    @DisplayName("Valid DTO: all filter fields are optional")
    void testValidDTOWithOptionalFields() {
        TransactionFilterRequestDTO dto = new TransactionFilterRequestDTO(
                UUID.randomUUID(),
                UserTransactionRole.ALL,
                new DateRange(LocalDateTime.now().minusDays(7), LocalDateTime.now()),
                "rent"
        );

        Set<ConstraintViolation<TransactionFilterRequestDTO>> violations = validator.validate(dto);

        assertTrue(violations.isEmpty(), "No validation errors expected for optional filter fields");
    }

    @Test
    @DisplayName("Valid DTO: null optional fields should not trigger violations")
    void testValidDTOWithNullOptionalFields() {
        TransactionFilterRequestDTO dto = new TransactionFilterRequestDTO(
                null,
                null,
                null,
                null
        );

        Set<ConstraintViolation<TransactionFilterRequestDTO>> violations = validator.validate(dto);

        assertTrue(violations.isEmpty(), "No validation errors expected when all optional fields are null");
    }
}
