package com.housemate.shared.dto.items;

import com.housemate.shared.dto.items.request.ShoppingListUpdateRequestDTO;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

@DisplayName("ShoppingListUpdateRequestDTO Validation Tests")
class ShoppingListUpdateRequestValidationTest {

    private static Validator validator;

    @BeforeAll
    static void setUpValidator() {
        ValidatorFactory factory = Validation.buildDefaultValidatorFactory();
        validator = factory.getValidator();
    }

    @Test
    @DisplayName("Valid DTO: boughtItems list is present")
    void testValidDTO() {
        ShoppingListUpdateRequestDTO dto = new ShoppingListUpdateRequestDTO(
                List.of(true, false, true)
        );

        Set<ConstraintViolation<ShoppingListUpdateRequestDTO>> violations = validator.validate(dto);

        assertTrue(violations.isEmpty(), "No validation errors expected for valid DTO");
    }

    @Test
    @DisplayName("@NotNull validation: boughtItems cannot be null")
    void testBoughtItemsNull() {
        ShoppingListUpdateRequestDTO dto = new ShoppingListUpdateRequestDTO(null);

        Set<ConstraintViolation<ShoppingListUpdateRequestDTO>> violations = validator.validate(dto);

        assertEquals(1, violations.size());
        assertEquals("boughtItems", violations.iterator().next().getPropertyPath().toString());
    }

    @Test
    @DisplayName("Valid DTO: empty boughtItems list")
    void testValidDTOWithEmptyList() {
        ShoppingListUpdateRequestDTO dto = new ShoppingListUpdateRequestDTO(List.of());

        Set<ConstraintViolation<ShoppingListUpdateRequestDTO>> violations = validator.validate(dto);

        assertTrue(violations.isEmpty(), "Empty boughtItems list should be valid");
    }

    @Test
    @DisplayName("Valid DTO: single item in boughtItems list")
    void testValidDTOWithSingleItem() {
        ShoppingListUpdateRequestDTO dto = new ShoppingListUpdateRequestDTO(List.of(true));

        Set<ConstraintViolation<ShoppingListUpdateRequestDTO>> violations = validator.validate(dto);

        assertTrue(violations.isEmpty(), "Single item in boughtItems list should be valid");
    }

    @Test
    @DisplayName("Valid DTO: many items in boughtItems list")
    void testValidDTOWithManyItems() {
        ShoppingListUpdateRequestDTO dto = new ShoppingListUpdateRequestDTO(
                List.of(true, false, true, false, true, false, true)
        );

        Set<ConstraintViolation<ShoppingListUpdateRequestDTO>> violations = validator.validate(dto);

        assertTrue(violations.isEmpty(), "Many items in boughtItems list should be valid");
    }

    @Test
    @DisplayName("Valid DTO: all items false")
    void testValidDTOWithAllFalse() {
        ShoppingListUpdateRequestDTO dto = new ShoppingListUpdateRequestDTO(
                List.of(false, false, false)
        );

        Set<ConstraintViolation<ShoppingListUpdateRequestDTO>> violations = validator.validate(dto);

        assertTrue(violations.isEmpty(), "All false items should be valid");
    }

    @Test
    @DisplayName("Valid DTO: all items true")
    void testValidDTOWithAllTrue() {
        ShoppingListUpdateRequestDTO dto = new ShoppingListUpdateRequestDTO(
                List.of(true, true, true)
        );

        Set<ConstraintViolation<ShoppingListUpdateRequestDTO>> violations = validator.validate(dto);

        assertTrue(violations.isEmpty(), "All true items should be valid");
    }
}

