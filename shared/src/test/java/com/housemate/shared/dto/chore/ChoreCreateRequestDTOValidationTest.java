package com.housemate.shared.dto.chore;

import com.housemate.shared.dto.chore.request.ChoreCreateRequestDTO;
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

@DisplayName("ChoreCreateRequestDTO Validation Tests")
class ChoreCreateRequestDTOValidationTest {

    private static Validator validator;

    @BeforeAll
    static void setUpValidator() {
        ValidatorFactory factory = Validation.buildDefaultValidatorFactory();
        validator = factory.getValidator();
    }

    @Test
    @DisplayName("DTO creation is successful: all data is valid")
    void testValidDTO() {
        ChoreCreateRequestDTO dto = new ChoreCreateRequestDTO(
                "Clean the kitchen",
                7,
                UUID.randomUUID()
        );

        Set<ConstraintViolation<ChoreCreateRequestDTO>> violations = validator.validate(dto);

        assertTrue(violations.isEmpty(), "No validation errors expected for a valid DTO");
    }

    @Test
    @DisplayName("DTO creation fails due to empty description")
    void testInvalidDescription() {
        ChoreCreateRequestDTO dto = new ChoreCreateRequestDTO(
                "",
                7,
                UUID.randomUUID()
        );

        Set<ConstraintViolation<ChoreCreateRequestDTO>> violations = validator.validate(dto);

        //both @NotBlank and @Pattern will trigger for an empty string, so we expect 2 violations
        assertEquals(2, violations.size());

        ConstraintViolation<ChoreCreateRequestDTO> violation = violations.iterator().next();
        assertEquals("description", violation.getPropertyPath().toString());
    }

    @Test
    @DisplayName("DTO creation fails due to negative frequency days")
    void testInvalidFrequency() {

        ChoreCreateRequestDTO dto = new ChoreCreateRequestDTO(
                "Clean the kitchen",
                -1,
                UUID.randomUUID()
        );

        Set<ConstraintViolation<ChoreCreateRequestDTO>> violations = validator.validate(dto);

        assertEquals(1, violations.size());
        assertEquals("frequencyDays", violations.iterator().next().getPropertyPath().toString());
    }

    @Test
    @DisplayName("@Pattern validation: description contains invalid characters")
    void testInvalidDescriptionPattern() {
        ChoreCreateRequestDTO dto = new ChoreCreateRequestDTO(
                "Clean @#$ the kitchen!",
                7,
                UUID.randomUUID()
        );

        Set<ConstraintViolation<ChoreCreateRequestDTO>> violations = validator.validate(dto);

        assertEquals(1, violations.size());
        assertEquals("description", violations.iterator().next().getPropertyPath().toString());
    }

    @Test
    @DisplayName("@NotNull validation: description cannot be null")
    void testNullDescription() {
        ChoreCreateRequestDTO dto = new ChoreCreateRequestDTO(
                null,
                7,
                UUID.randomUUID()
        );

        Set<ConstraintViolation<ChoreCreateRequestDTO>> violations = validator.validate(dto);

        // @NotNull and @NotBlank will both trigger
        assertEquals(2, violations.size());
        ConstraintViolation<ChoreCreateRequestDTO> violation = violations.iterator().next();
        assertEquals("description", violation.getPropertyPath().toString());
    }

    @Test
    @DisplayName("@NotNull validation: frequencyDays cannot be null")
    void testNullFrequencyDays() {
        ChoreCreateRequestDTO dto = new ChoreCreateRequestDTO(
                "Clean the kitchen",
                null,
                UUID.randomUUID()
        );

        Set<ConstraintViolation<ChoreCreateRequestDTO>> violations = validator.validate(dto);

        assertEquals(1, violations.size());
        assertEquals("frequencyDays", violations.iterator().next().getPropertyPath().toString());
    }

    @Test
    @DisplayName("@NotNull validation: householdId cannot be null")
    void testNullHouseholdId() {
        ChoreCreateRequestDTO dto = new ChoreCreateRequestDTO(
                "Clean the kitchen",
                7,
                null
        );

        Set<ConstraintViolation<ChoreCreateRequestDTO>> violations = validator.validate(dto);

        assertEquals(1, violations.size());
        assertEquals("householdId", violations.iterator().next().getPropertyPath().toString());
    }
}