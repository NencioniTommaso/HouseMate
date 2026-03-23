package com.housemate.client.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.housemate.shared.dto.items.request.ShoppingListCreateRequestDTO;
import com.housemate.shared.dto.items.request.ShoppingListUpdateRequestDTO;
import com.housemate.shared.dto.items.response.ShoppingListResponseDTO;
import com.housemate.shared.enums.ShoppingListStatus;
import com.housemate.shared.utils.types.ListItem;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@DisplayName("ShoppingListClientService Unit Tests")
class ShoppingListClientServiceTest {

    // ============ Injected Dependencies ============
    private ShoppingListClientService shoppingListClientService;
    private HttpRestClient mockHttpRestClient;
    private ObjectMapper objectMapper;

    // ============ Test Data Constants ============
    private static final UUID TEST_SHOPPING_LIST_ID = UUID.fromString("00000000-0000-0000-0000-000000000101");
    private static final UUID TEST_HOUSEHOLD_ID = UUID.fromString("00000000-0000-0000-0000-000000000102");

    private static final String TEST_LIST_NAME = "Weekly Groceries";

    // ============ Test Objects ============
    private ShoppingListResponseDTO testShoppingListResponseDTO;
    private ShoppingListCreateRequestDTO testCreateRequestDTO;
    private ShoppingListUpdateRequestDTO testUpdateRequestDTO;
    private List<ListItem> testListItems;

    @BeforeEach
    void setUp() {
        objectMapper = new ObjectMapper().registerModule(new JavaTimeModule());
        mockHttpRestClient = mock(HttpRestClient.class);
        shoppingListClientService = new ShoppingListClientService(mockHttpRestClient);

        testListItems = createTestListItems();
        testShoppingListResponseDTO = createTestShoppingListResponseDTO();
        testCreateRequestDTO = createTestCreateRequestDTO();
        testUpdateRequestDTO = createTestUpdateRequestDTO();
    }

    // ============ Helper Methods for Test Data ============

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
                TEST_HOUSEHOLD_ID
        );
    }

    private ShoppingListCreateRequestDTO createTestCreateRequestDTO() {
        return new ShoppingListCreateRequestDTO(
                TEST_LIST_NAME,
                testListItems,
                TEST_HOUSEHOLD_ID
        );
    }

    private ShoppingListUpdateRequestDTO createTestUpdateRequestDTO() {
        return new ShoppingListUpdateRequestDTO(List.of(true, false, true));
    }

    // ============ Helper Methods for HTTP Mocking ============

    @SuppressWarnings("unchecked")
    private <T> HttpResponse<T> createMockResponse(int statusCode, T body) {
        HttpResponse<T> response = (HttpResponse<T>) mock(HttpResponse.class);
        when(response.statusCode()).thenReturn(statusCode);
        when(response.body()).thenReturn(body);
        return response;
    }

    // ============ Tests for createShoppingList ============

    @Test
    @DisplayName("createShoppingList - should successfully create a shopping list and return ShoppingListResponseDTO")
    void testCreateShoppingList_Success() throws IOException {

        String jsonResponse = objectMapper.writeValueAsString(testShoppingListResponseDTO);

        HttpResponse<String> mockResponse = createMockResponse(201, jsonResponse);

        when(mockHttpRestClient.serializeDTO(any())).thenReturn("{\"name\":\"test\"}");
        when(mockHttpRestClient.sendRequest(any(HttpRequest.class))).thenReturn(mockResponse);
        when(mockHttpRestClient.deserializeDTO(jsonResponse, ShoppingListResponseDTO.class))
                .thenReturn(testShoppingListResponseDTO);
        when(mockHttpRestClient.buildAuthHeader()).thenReturn("Bearer test-token");

        ShoppingListResponseDTO result = shoppingListClientService.createShoppingList(testCreateRequestDTO);

        assertNotNull(result);
        assertEquals(TEST_SHOPPING_LIST_ID, result.id());
        assertEquals(TEST_LIST_NAME, result.name());
        assertEquals(3, result.items().size());

        verify(mockHttpRestClient).serializeDTO(any());
        verify(mockHttpRestClient).sendRequest(any(HttpRequest.class));
        verify(mockHttpRestClient).deserializeDTO(jsonResponse, ShoppingListResponseDTO.class);
    }

    @Test
    @DisplayName("createShoppingList - should throw RuntimeException when server returns error status code")
    void testCreateShoppingList_ServerError() {

        HttpResponse<String> mockResponse = createMockResponse(400, "Household not found");
        when(mockHttpRestClient.serializeDTO(any())).thenReturn("{\"name\":\"test\"}");
        when(mockHttpRestClient.sendRequest(any(HttpRequest.class))).thenReturn(mockResponse);
        when(mockHttpRestClient.buildAuthHeader()).thenReturn("Bearer test-token");

        RuntimeException exception = assertThrows(RuntimeException.class,
                () -> shoppingListClientService.createShoppingList(testCreateRequestDTO));


        assertTrue(exception.getMessage().contains("Failed to create shopping list"));
    }

    // ============ Tests for deleteShoppingList ============

    @Test
    @DisplayName("deleteShoppingList - should successfully delete a shopping list with status 204")
    void testDeleteShoppingList_Success() {

        HttpResponse<String> mockResponse = createMockResponse(204, "");
        when(mockHttpRestClient.sendRequest(any(HttpRequest.class))).thenReturn(mockResponse);
        when(mockHttpRestClient.buildAuthHeader()).thenReturn("Bearer test-token");

        assertDoesNotThrow(() -> shoppingListClientService.deleteShoppingList(TEST_SHOPPING_LIST_ID));

        verify(mockHttpRestClient).sendRequest(any(HttpRequest.class));
    }

    @Test
    @DisplayName("deleteShoppingList - should throw RuntimeException when server returns error status code")
    void testDeleteShoppingList_ServerError() {

        HttpResponse<String> mockResponse = createMockResponse(404, "Shopping list not found");
        when(mockHttpRestClient.sendRequest(any(HttpRequest.class))).thenReturn(mockResponse);
        when(mockHttpRestClient.buildAuthHeader()).thenReturn("Bearer test-token");

        RuntimeException exception = assertThrows(RuntimeException.class,
                () -> shoppingListClientService.deleteShoppingList(TEST_SHOPPING_LIST_ID));

        assertTrue(exception.getMessage().contains("Failed to delete shopping list"));
    }

    // ============ Tests for updateListInformation ============

    @Test
    @DisplayName("updateListInformation - should successfully update list information and return ShoppingListResponseDTO")
    void testUpdateListInformation_Success() throws IOException {

        String jsonResponse = objectMapper.writeValueAsString(testShoppingListResponseDTO);

        HttpResponse<String> mockResponse = createMockResponse(200, jsonResponse);

        when(mockHttpRestClient.serializeDTO(any())).thenReturn("{\"boughtItems\":[true,false,true]}");
        when(mockHttpRestClient.sendRequest(any(HttpRequest.class))).thenReturn(mockResponse);
        when(mockHttpRestClient.deserializeDTO(jsonResponse, ShoppingListResponseDTO.class))
                .thenReturn(testShoppingListResponseDTO);
        when(mockHttpRestClient.buildAuthHeader()).thenReturn("Bearer test-token");

        ShoppingListResponseDTO result = shoppingListClientService.updateListInformation(TEST_SHOPPING_LIST_ID, testUpdateRequestDTO);

        assertNotNull(result);
        assertEquals(TEST_SHOPPING_LIST_ID, result.id());
        assertEquals(TEST_LIST_NAME, result.name());

        verify(mockHttpRestClient).serializeDTO(any());
        verify(mockHttpRestClient).sendRequest(any(HttpRequest.class));
        verify(mockHttpRestClient).deserializeDTO(jsonResponse, ShoppingListResponseDTO.class);
    }

    @Test
    @DisplayName("updateListInformation - should throw RuntimeException when server returns error status code")
    void testUpdateListInformation_ServerError() {

        HttpResponse<String> mockResponse = createMockResponse(400, "Invalid request");
        when(mockHttpRestClient.serializeDTO(any())).thenReturn("{\"boughtItems\":[true,false,true]}");
        when(mockHttpRestClient.sendRequest(any(HttpRequest.class))).thenReturn(mockResponse);
        when(mockHttpRestClient.buildAuthHeader()).thenReturn("Bearer test-token");

        RuntimeException exception = assertThrows(RuntimeException.class,
                () -> shoppingListClientService.updateListInformation(TEST_SHOPPING_LIST_ID, testUpdateRequestDTO));

        assertTrue(exception.getMessage().contains("Failed to update shopping list"));
    }

    // ============ Tests for getShoppingItemsByHousehold ============

    @Test
    @DisplayName("getShoppingItemsByHousehold - should successfully retrieve shopping lists and return list of ShoppingListResponseDTO")
    void testGetShoppingItemsByHousehold_Success() throws IOException {

        List<ShoppingListResponseDTO> listResponse = List.of(testShoppingListResponseDTO);
        String jsonResponse = objectMapper.writeValueAsString(listResponse);

        HttpResponse<String> mockResponse = createMockResponse(200, jsonResponse);

        when(mockHttpRestClient.sendRequest(any(HttpRequest.class))).thenReturn(mockResponse);
        when(mockHttpRestClient.deserializeDTOList(jsonResponse, ShoppingListResponseDTO.class))
                .thenReturn(listResponse);
        when(mockHttpRestClient.buildAuthHeader()).thenReturn("Bearer test-token");

        List<ShoppingListResponseDTO> result = shoppingListClientService.getShoppingItemsByHousehold(TEST_HOUSEHOLD_ID);

        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals(TEST_LIST_NAME, result.get(0).name());

        verify(mockHttpRestClient).sendRequest(any(HttpRequest.class));
        verify(mockHttpRestClient).deserializeDTOList(jsonResponse, ShoppingListResponseDTO.class);
    }

    @Test
    @DisplayName("getShoppingItemsByHousehold - should return empty list when household has no shopping lists")
    void testGetShoppingItemsByHousehold_EmptyList() throws IOException {

        List<ShoppingListResponseDTO> listResponse = new ArrayList<>();
        String jsonResponse = objectMapper.writeValueAsString(listResponse);

        HttpResponse<String> mockResponse = createMockResponse(200, jsonResponse);

        when(mockHttpRestClient.sendRequest(any(HttpRequest.class))).thenReturn(mockResponse);
        when(mockHttpRestClient.deserializeDTOList(jsonResponse, ShoppingListResponseDTO.class))
                .thenReturn(listResponse);
        when(mockHttpRestClient.buildAuthHeader()).thenReturn("Bearer test-token");

        List<ShoppingListResponseDTO> result = shoppingListClientService.getShoppingItemsByHousehold(TEST_HOUSEHOLD_ID);

        assertNotNull(result);
        assertEquals(0, result.size());

        verify(mockHttpRestClient).sendRequest(any(HttpRequest.class));
        verify(mockHttpRestClient).deserializeDTOList(jsonResponse, ShoppingListResponseDTO.class);
    }

    @Test
    @DisplayName("getShoppingItemsByHousehold - should throw RuntimeException when server returns error status code")
    void testGetShoppingItemsByHousehold_ServerError() {

        HttpResponse<String> mockResponse = createMockResponse(404, "Household not found");
        when(mockHttpRestClient.sendRequest(any(HttpRequest.class))).thenReturn(mockResponse);
        when(mockHttpRestClient.buildAuthHeader()).thenReturn("Bearer test-token");

        RuntimeException exception = assertThrows(RuntimeException.class,
                () -> shoppingListClientService.getShoppingItemsByHousehold(TEST_HOUSEHOLD_ID));

        assertTrue(exception.getMessage().contains("Failed to retrieve shopping lists for household"));
    }
}



