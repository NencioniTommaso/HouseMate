package com.housemate.shared.dto.items;

import com.housemate.shared.dto.items.response.ShoppingListResponseDTO;
import com.housemate.shared.enums.ShoppingListStatus;
import com.housemate.shared.utils.types.ListItem;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertTrue;

@DisplayName("ShoppingListResponseDTO Validation Tests")
class ShoppingListResponseValidationTest {

    private static Validator validator;
    private static final UUID TEST_SHOPPING_LIST_ID = UUID.fromString("00000000-0000-0000-0000-000000000101");
    private static final UUID TEST_HOUSEHOLD_ID = UUID.fromString("00000000-0000-0000-0000-000000000102");

    @BeforeAll
    static void setUpValidator() {
        ValidatorFactory factory = Validation.buildDefaultValidatorFactory();
        validator = factory.getValidator();
    }

    private List<ListItem> createTestItems() {
        List<ListItem> items = new ArrayList<>();
        ListItem item1 = new ListItem("Milk");
        item1.setBought(false);
        items.add(item1);
        
        ListItem item2 = new ListItem("Bread");
        item2.setBought(false);
        items.add(item2);
        
        return items;
    }

    @Test
    @DisplayName("Valid DTO: all fields are properly populated")
    void testValidDTO() {
        List<ListItem> items = createTestItems();
        ShoppingListResponseDTO dto = new ShoppingListResponseDTO(
                TEST_SHOPPING_LIST_ID,
                "Weekly Groceries",
                items,
                ShoppingListStatus.NOT_STARTED,
                TEST_HOUSEHOLD_ID
        );

        Set<ConstraintViolation<ShoppingListResponseDTO>> violations = validator.validate(dto);

        assertTrue(violations.isEmpty(), "No validation errors expected for valid DTO");
    }

    @Test
    @DisplayName("Valid DTO: with empty items list")
    void testValidDTOWithEmptyItems() {
        ShoppingListResponseDTO dto = new ShoppingListResponseDTO(
                TEST_SHOPPING_LIST_ID,
                "Weekly Groceries",
                new ArrayList<>(),
                ShoppingListStatus.NOT_STARTED,
                TEST_HOUSEHOLD_ID
        );

        Set<ConstraintViolation<ShoppingListResponseDTO>> violations = validator.validate(dto);

        assertTrue(violations.isEmpty(), "No validation errors expected for DTO with empty items");
    }

    @Test
    @DisplayName("Valid DTO: with COMPLETED status")
    void testValidDTOWithCompletedStatus() {
        List<ListItem> items = new ArrayList<>();
        ListItem item1 = new ListItem("Milk");
        item1.setBought(true);
        items.add(item1);
        
        ListItem item2 = new ListItem("Bread");
        item2.setBought(true);
        items.add(item2);
        
        ShoppingListResponseDTO dto = new ShoppingListResponseDTO(
                TEST_SHOPPING_LIST_ID,
                "Weekly Groceries",
                items,
                ShoppingListStatus.COMPLETED,
                TEST_HOUSEHOLD_ID
        );

        Set<ConstraintViolation<ShoppingListResponseDTO>> violations = validator.validate(dto);

        assertTrue(violations.isEmpty(), "No validation errors expected for DTO with COMPLETED status");
    }

    @Test
    @DisplayName("Valid DTO: with IN_PROGRESS status")
    void testValidDTOWithInProgressStatus() {
        List<ListItem> items = new ArrayList<>();
        ListItem item1 = new ListItem("Milk");
        item1.setBought(true);
        items.add(item1);
        
        ListItem item2 = new ListItem("Bread");
        item2.setBought(false);
        items.add(item2);
        
        ShoppingListResponseDTO dto = new ShoppingListResponseDTO(
                TEST_SHOPPING_LIST_ID,
                "Weekly Groceries",
                items,
                ShoppingListStatus.IN_PROGRESS,
                TEST_HOUSEHOLD_ID
        );

        Set<ConstraintViolation<ShoppingListResponseDTO>> violations = validator.validate(dto);

        assertTrue(violations.isEmpty(), "No validation errors expected for DTO with IN_PROGRESS status");
    }

    @Test
    @DisplayName("Valid DTO: with different numeric list names")
    void testValidDTOWithNumericName() {
        List<ListItem> items = new ArrayList<>();
        ListItem item = new ListItem("Milk");
        item.setBought(false);
        items.add(item);
        
        ShoppingListResponseDTO dto = new ShoppingListResponseDTO(
                TEST_SHOPPING_LIST_ID,
                "123 456",
                items,
                ShoppingListStatus.NOT_STARTED,
                TEST_HOUSEHOLD_ID
        );

        Set<ConstraintViolation<ShoppingListResponseDTO>> violations = validator.validate(dto);

        assertTrue(violations.isEmpty(), "Numeric list names should be valid");
    }

    @Test
    @DisplayName("Valid DTO: with many items")
    void testValidDTOWithManyItems() {
        List<ListItem> items = new ArrayList<>();
        String[] itemNames = {"Milk", "Bread", "Eggs", "Cheese", "Butter", "Yogurt"};
        boolean[] boughtStatus = {true, true, false, true, false, false};
        
        for (int i = 0; i < itemNames.length; i++) {
            ListItem item = new ListItem(itemNames[i]);
            item.setBought(boughtStatus[i]);
            items.add(item);
        }
        
        ShoppingListResponseDTO dto = new ShoppingListResponseDTO(
                TEST_SHOPPING_LIST_ID,
                "Complete Weekly Groceries",
                items,
                ShoppingListStatus.IN_PROGRESS,
                TEST_HOUSEHOLD_ID
        );

        Set<ConstraintViolation<ShoppingListResponseDTO>> violations = validator.validate(dto);

        assertTrue(violations.isEmpty(), "DTO with many items should be valid");
    }

    @Test
    @DisplayName("Valid DTO: record with different UUIDs")
    void testValidDTOWithDifferentUUIDs() {
        UUID differentListId = UUID.randomUUID();
        UUID differentHouseholdId = UUID.randomUUID();
        
        List<ListItem> items = new ArrayList<>();
        ListItem item = new ListItem("Milk");
        item.setBought(false);
        items.add(item);
        
        ShoppingListResponseDTO dto = new ShoppingListResponseDTO(
                differentListId,
                "Shopping List",
                items,
                ShoppingListStatus.NOT_STARTED,
                differentHouseholdId
        );

        Set<ConstraintViolation<ShoppingListResponseDTO>> violations = validator.validate(dto);

        assertTrue(violations.isEmpty(), "Different UUIDs should be valid");
    }
}

