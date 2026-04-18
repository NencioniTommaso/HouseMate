package com.housemate.backend.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.housemate.backend.service.ShoppingListService;
import com.housemate.shared.dto.items.request.ShoppingListCreateRequestDTO;
import com.housemate.shared.dto.items.request.ShoppingListUpdateRequestDTO;
import com.housemate.shared.dto.items.response.ShoppingListResponseDTO;
import com.housemate.shared.enums.ShoppingListStatus;
import com.housemate.shared.utils.types.ListItem;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import static org.mockito.Mockito.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(ShoppingListController.class)
@DisplayName("ShoppingListController Integration Tests")
@WithMockUser(username = "00000000-0000-0000-0000-000000000103")
class ShoppingListControllerTest {

    // ============ Injected Dependencies ============
    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private ShoppingListService shoppingListService;

    // ============ Test Data Constants ============
    private static final UUID TEST_SHOPPING_LIST_ID = UUID.fromString("00000000-0000-0000-0000-000000000101");
    private static final UUID TEST_HOUSEHOLD_ID = UUID.fromString("00000000-0000-0000-0000-000000000102");
    private static final UUID TEST_USER_ID = UUID.fromString("00000000-0000-0000-0000-000000000103");
    private static final LocalDate TEST_CREATION_DATE = LocalDate.of(2025, 1, 15);

    private static final String TEST_LIST_NAME = "Weekly Groceries";

    // ============ Test Objects ============
    private ShoppingListResponseDTO testShoppingListResponseDTO;
    private ShoppingListCreateRequestDTO testCreateRequestDTO;
    private ShoppingListUpdateRequestDTO testUpdateRequestDTO;
    private List<ListItem> testListItems;

    @BeforeEach
    void setUp() {
        testListItems = createTestListItems();
        testShoppingListResponseDTO = createTestShoppingListResponseDTO();
        testCreateRequestDTO = createTestCreateRequestDTO();
        testUpdateRequestDTO = createTestUpdateRequestDTO();
    }

    // ============ Helper Methods ============

    private List<ListItem> createTestListItems() {
        List<ListItem> items = new ArrayList<>();
        ListItem item1 = new ListItem("Milk");
        item1.setBought(false);
        items.add(item1);

        ListItem item2 = new ListItem("Bread");
        item2.setBought(false);
        items.add(item2);

        ListItem item3 = new ListItem("Eggs");
        item3.setBought(false);
        items.add(item3);

        return items;
    }

    private ShoppingListResponseDTO createTestShoppingListResponseDTO() {
        return new ShoppingListResponseDTO(
                TEST_SHOPPING_LIST_ID,
                TEST_LIST_NAME,
                testListItems,
                ShoppingListStatus.NOT_STARTED,
                TEST_HOUSEHOLD_ID,
                TEST_CREATION_DATE
        );
    }

    private ShoppingListCreateRequestDTO createTestCreateRequestDTO() {
        return new ShoppingListCreateRequestDTO(
                TEST_LIST_NAME,
                testListItems,
                TEST_HOUSEHOLD_ID,
                TEST_CREATION_DATE
        );
    }

    private ShoppingListUpdateRequestDTO createTestUpdateRequestDTO() {
        return new ShoppingListUpdateRequestDTO(List.of(true, false, true));
    }

    // ============ Tests for POST /api/shopping-lists ============

    @Test
    @DisplayName("POST /api/shopping-lists - should return 201 Created with ShoppingListResponseDTO on valid input")
    void testCreateShoppingList_Success() throws Exception {

        when(shoppingListService.createShoppingList(eq(TEST_USER_ID), any(ShoppingListCreateRequestDTO.class)))
                .thenReturn(testShoppingListResponseDTO);

        mockMvc.perform(post("/api/shopping-lists")
                        .with(csrf())
                        .contentType("application/json")
                        .content(objectMapper.writeValueAsString(testCreateRequestDTO)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(TEST_SHOPPING_LIST_ID.toString()))
                .andExpect(jsonPath("$.name").value(TEST_LIST_NAME))
                .andExpect(jsonPath("$.status").value("NOT_STARTED"))
                .andExpect(jsonPath("$.items").isArray())
                .andExpect(jsonPath("$.items.length()").value(3))
                .andExpect(jsonPath("$.creationDate").value(TEST_CREATION_DATE.toString()));

        verify(shoppingListService).createShoppingList(eq(TEST_USER_ID), any(ShoppingListCreateRequestDTO.class));
    }

    @Test
    @DisplayName("POST /api/shopping-lists - should return 400 Bad Request on invalid request body")
    void testCreateShoppingList_InvalidInput() throws Exception {

        ShoppingListCreateRequestDTO invalidRequestDTO = new ShoppingListCreateRequestDTO(
                "",
                testListItems,
                TEST_HOUSEHOLD_ID,
                TEST_CREATION_DATE
        );

        mockMvc.perform(post("/api/shopping-lists")
                        .with(csrf())
                        .contentType("application/json")
                        .content(objectMapper.writeValueAsString(invalidRequestDTO)))
                .andExpect(status().isBadRequest());

        verify(shoppingListService, never()).createShoppingList(any(UUID.class), any(ShoppingListCreateRequestDTO.class));
    }

    @Test
    @DisplayName("POST /api/shopping-lists - should return 400 Bad Request when service throws IllegalArgumentException")
    void testCreateShoppingList_ServiceError() throws Exception {

        when(shoppingListService.createShoppingList(eq(TEST_USER_ID), any(ShoppingListCreateRequestDTO.class)))
                .thenThrow(new IllegalArgumentException("Household not found"));

        mockMvc.perform(post("/api/shopping-lists")
                        .with(csrf())
                        .contentType("application/json")
                        .content(objectMapper.writeValueAsString(testCreateRequestDTO)))
                .andExpect(status().isBadRequest());

        verify(shoppingListService).createShoppingList(eq(TEST_USER_ID), any(ShoppingListCreateRequestDTO.class));
    }

    // ============ Tests for DELETE /api/shopping-lists/{listId} ============

    @Test
    @DisplayName("DELETE /api/shopping-lists/{listId} - should return 204 No Content on successful deletion")
    void testDeleteShoppingList_Success() throws Exception {

        doNothing().when(shoppingListService).deleteShoppingList(TEST_USER_ID, TEST_SHOPPING_LIST_ID);

        mockMvc.perform(delete("/api/shopping-lists/{listId}", TEST_SHOPPING_LIST_ID)
                        .with(csrf()))
                .andExpect(status().isNoContent());

        verify(shoppingListService).deleteShoppingList(TEST_USER_ID, TEST_SHOPPING_LIST_ID);
    }

    @Test
    @DisplayName("DELETE /api/shopping-lists/{listId} - should return 400 Bad Request when service throws IllegalArgumentException")
    void testDeleteShoppingList_NotFound() throws Exception {

        doThrow(new IllegalArgumentException("Shopping list not found"))
                .when(shoppingListService).deleteShoppingList(TEST_USER_ID, TEST_SHOPPING_LIST_ID);

        mockMvc.perform(delete("/api/shopping-lists/{listId}", TEST_SHOPPING_LIST_ID)
                        .with(csrf()))
                .andExpect(status().isBadRequest());

        verify(shoppingListService).deleteShoppingList(TEST_USER_ID, TEST_SHOPPING_LIST_ID);
    }

    // ============ Tests for PATCH /api/shopping-lists/{listId} ============

    @Test
    @DisplayName("PATCH /api/shopping-lists/{listId} - should return 200 OK with updated ShoppingListResponseDTO on valid input")
    void testUpdateListStatus_Success() throws Exception {

        when(shoppingListService.updateShoppingList(eq(TEST_USER_ID), eq(TEST_SHOPPING_LIST_ID), any(ShoppingListUpdateRequestDTO.class)))
                .thenReturn(testShoppingListResponseDTO);

        mockMvc.perform(patch("/api/shopping-lists/{listId}", TEST_SHOPPING_LIST_ID)
                        .with(csrf())
                        .contentType("application/json")
                        .content(objectMapper.writeValueAsString(testUpdateRequestDTO)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(TEST_SHOPPING_LIST_ID.toString()))
                .andExpect(jsonPath("$.name").value(TEST_LIST_NAME))
                .andExpect(jsonPath("$.creationDate").value(TEST_CREATION_DATE.toString()));

        verify(shoppingListService).updateShoppingList(eq(TEST_USER_ID), eq(TEST_SHOPPING_LIST_ID), any(ShoppingListUpdateRequestDTO.class));
    }

    @Test
    @DisplayName("PATCH /api/shopping-lists/{listId} - should return 400 Bad Request when service throws IllegalArgumentException")
    void testUpdateListStatus_NotFound() throws Exception {

        when(shoppingListService.updateShoppingList(eq(TEST_USER_ID), eq(TEST_SHOPPING_LIST_ID), any(ShoppingListUpdateRequestDTO.class)))
                .thenThrow(new IllegalArgumentException("Shopping list not found"));

        mockMvc.perform(patch("/api/shopping-lists/{listId}", TEST_SHOPPING_LIST_ID)
                        .with(csrf())
                        .contentType("application/json")
                        .content(objectMapper.writeValueAsString(testUpdateRequestDTO)))
                .andExpect(status().isBadRequest());

        verify(shoppingListService).updateShoppingList(eq(TEST_USER_ID), eq(TEST_SHOPPING_LIST_ID), any(ShoppingListUpdateRequestDTO.class));
    }

    // ============ Tests for GET /api/shopping-lists ============

    @Test
    @DisplayName("GET /api/shopping-lists - should return 200 OK with list of ShoppingListResponseDTO")
    void testGetShoppingListsByHousehold_Success() throws Exception {

        List<ShoppingListResponseDTO> responseList = List.of(testShoppingListResponseDTO);

        when(shoppingListService.getShoppingListsByHousehold(TEST_USER_ID))
                .thenReturn(responseList);

        mockMvc.perform(get("/api/shopping-lists"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$[0].id").value(TEST_SHOPPING_LIST_ID.toString()))
                .andExpect(jsonPath("$[0].name").value(TEST_LIST_NAME))
                .andExpect(jsonPath("$[0].householdId").value(TEST_HOUSEHOLD_ID.toString()))
                .andExpect(jsonPath("$[0].creationDate").value(TEST_CREATION_DATE.toString()));

        verify(shoppingListService).getShoppingListsByHousehold(TEST_USER_ID);
    }

    @Test
    @DisplayName("GET /api/shopping-lists - should return 200 OK with empty list when household has no shopping lists")
    void testGetShoppingListsByHousehold_EmptyList() throws Exception {

        when(shoppingListService.getShoppingListsByHousehold(TEST_USER_ID))
                .thenReturn(new ArrayList<>());

        mockMvc.perform(get("/api/shopping-lists"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$").isEmpty());

        verify(shoppingListService).getShoppingListsByHousehold(TEST_USER_ID);
    }

    @Test
    @DisplayName("GET /api/shopping-lists - should return 400 Bad Request when service throws IllegalArgumentException")
    void testGetShoppingListsByHousehold_ServiceError() throws Exception {

        when(shoppingListService.getShoppingListsByHousehold(TEST_USER_ID))
                .thenThrow(new IllegalArgumentException("User not found"));

        mockMvc.perform(get("/api/shopping-lists"))
                .andExpect(status().isBadRequest());

        verify(shoppingListService).getShoppingListsByHousehold(TEST_USER_ID);
    }
}
