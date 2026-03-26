package com.housemate.shared.dto.items;

import com.housemate.shared.dto.items.request.ShoppingListCreateRequestDTO;
import com.housemate.shared.utils.types.ListItem;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

@DisplayName("ShoppingListCreateRequestDTO Validation Tests")
class ShoppingListCreateRequestDTOValidationTest {

    private static Validator validator;
    private static final UUID TEST_HOUSEHOLD_ID = UUID.fromString("00000000-0000-0000-0000-000000000102");
    private static final LocalDate TEST_CREATION_DATE = LocalDate.of(2025, 1, 15);

    @BeforeAll
    static void setUpValidator() {
        ValidatorFactory factory = Validation.buildDefaultValidatorFactory();
        validator = factory.getValidator();
    }

    private List<ListItem> createTestItems() {
        List<ListItem> items = new ArrayList<>();
        ListItem item1 = new ListItem("Milk");
        items.add(item1);
        
        ListItem item2 = new ListItem("Bread");
        items.add(item2);
        
        return items;
    }

    @Test
    @DisplayName("Valid DTO: all fields are properly populated")
    void testValidDTO() {
        List<ListItem> items = createTestItems();
        ShoppingListCreateRequestDTO dto = new ShoppingListCreateRequestDTO(
                "Weekly Groceries",
                items,
                TEST_HOUSEHOLD_ID,
                TEST_CREATION_DATE
        );

        Set<ConstraintViolation<ShoppingListCreateRequestDTO>> violations = validator.validate(dto);

        assertTrue(violations.isEmpty(), "No validation errors expected for valid DTO");
    }

    @Test
    @DisplayName("@NotNull validation: name cannot be null")
    void testNameNull() {
        List<ListItem> items = createTestItems();
        ShoppingListCreateRequestDTO dto = new ShoppingListCreateRequestDTO(
                null,
                items,
                TEST_HOUSEHOLD_ID,
                TEST_CREATION_DATE
        );

        Set<ConstraintViolation<ShoppingListCreateRequestDTO>> violations = validator.validate(dto);

        assertEquals(2, violations.size());
        assertTrue(violations.stream().anyMatch(v -> v.getPropertyPath().toString().equals("name")));
    }

    @Test
    @DisplayName("@NotBlank validation: name cannot be blank")
    void testNameBlank() {
        List<ListItem> items = createTestItems();
        ShoppingListCreateRequestDTO dto = new ShoppingListCreateRequestDTO(
                "   ",
                items,
                TEST_HOUSEHOLD_ID,
                TEST_CREATION_DATE
        );

        Set<ConstraintViolation<ShoppingListCreateRequestDTO>> violations = validator.validate(dto);

        assertEquals(1, violations.size());
        assertTrue(violations.stream().anyMatch(v -> v.getPropertyPath().toString().equals("name")));
    }

    @Test
    @DisplayName("@Pattern validation: name can only contain letters, numbers, and spaces")
    void testNameInvalidPattern() {
        List<ListItem> items = createTestItems();
        ShoppingListCreateRequestDTO dto = new ShoppingListCreateRequestDTO(
                "Weekly@Groceries#",
                items,
                TEST_HOUSEHOLD_ID,
                TEST_CREATION_DATE
        );

        Set<ConstraintViolation<ShoppingListCreateRequestDTO>> violations = validator.validate(dto);

        assertEquals(1, violations.size());
        assertTrue(violations.stream().anyMatch(v -> v.getPropertyPath().toString().equals("name")));
    }

    @Test
    @DisplayName("@Pattern validation: name can contain letters, numbers, and spaces")
    void testNameValidPattern() {
        List<ListItem> items = createTestItems();
        ShoppingListCreateRequestDTO dto = new ShoppingListCreateRequestDTO(
                "Weekly Groceries 123",
                items,
                TEST_HOUSEHOLD_ID,
                TEST_CREATION_DATE
        );

        Set<ConstraintViolation<ShoppingListCreateRequestDTO>> violations = validator.validate(dto);

        assertTrue(violations.isEmpty(), "Name with letters, numbers, and spaces should be valid");
    }

    @Test
    @DisplayName("@NotNull validation: items cannot be null")
    void testItemsNull() {
        ShoppingListCreateRequestDTO dto = new ShoppingListCreateRequestDTO(
                "Weekly Groceries",
                null,
                TEST_HOUSEHOLD_ID,
                TEST_CREATION_DATE
        );

        Set<ConstraintViolation<ShoppingListCreateRequestDTO>> violations = validator.validate(dto);

        assertEquals(1, violations.size());
        assertEquals("items", violations.iterator().next().getPropertyPath().toString());
    }

    @Test
    @DisplayName("@NotNull validation: householdId cannot be null")
    void testHouseholdIdNull() {
        List<ListItem> items = createTestItems();
        ShoppingListCreateRequestDTO dto = new ShoppingListCreateRequestDTO(
                "Weekly Groceries",
                items,
                null,
                TEST_CREATION_DATE
        );

        Set<ConstraintViolation<ShoppingListCreateRequestDTO>> violations = validator.validate(dto);

        assertEquals(1, violations.size());
        assertEquals("householdId", violations.iterator().next().getPropertyPath().toString());
    }

    @Test
    @DisplayName("Multiple validation errors: name null and items null")
    void testMultipleValidationErrors() {
        ShoppingListCreateRequestDTO dto = new ShoppingListCreateRequestDTO(
                null,
                null,
                TEST_HOUSEHOLD_ID,
                TEST_CREATION_DATE
        );

        Set<ConstraintViolation<ShoppingListCreateRequestDTO>> violations = validator.validate(dto);

        assertEquals(3, violations.size());
        assertTrue(violations.stream().anyMatch(v -> v.getPropertyPath().toString().equals("name")));
        assertTrue(violations.stream().anyMatch(v -> v.getPropertyPath().toString().equals("items")));
    }

    @Test
    @DisplayName("Valid DTO with empty items list")
    void testValidDTOWithEmptyItems() {
        ShoppingListCreateRequestDTO dto = new ShoppingListCreateRequestDTO(
                "Weekly Groceries",
                List.of(),
                TEST_HOUSEHOLD_ID,
                TEST_CREATION_DATE
        );

        Set<ConstraintViolation<ShoppingListCreateRequestDTO>> violations = validator.validate(dto);

        assertTrue(violations.isEmpty(), "Empty items list should be valid");
    }

    @Test
    @DisplayName("Valid DTO with many items")
    void testValidDTOWithManyItems() {
        List<ListItem> items = new ArrayList<>();
        String[] itemNames = {"Milk", "Bread", "Eggs", "Cheese", "Butter"};
        for (String itemName : itemNames) {
            ListItem item = new ListItem(itemName);
            items.add(item);
        }
        ShoppingListCreateRequestDTO dto = new ShoppingListCreateRequestDTO(
                "Weekly Groceries",
                items,
                TEST_HOUSEHOLD_ID,
                TEST_CREATION_DATE
        );

        Set<ConstraintViolation<ShoppingListCreateRequestDTO>> violations = validator.validate(dto);

        assertTrue(violations.isEmpty(), "DTO with many items should be valid");
    }
}

