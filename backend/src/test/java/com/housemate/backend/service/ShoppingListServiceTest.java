package com.housemate.backend.service;

import com.housemate.backend.model.household.Household;
import com.housemate.backend.model.household.HouseholdMembership;
import com.housemate.backend.model.items.ShoppingList;
import com.housemate.backend.model.user.User;
import com.housemate.backend.repository.household.HouseholdRepository;
import com.housemate.backend.repository.items.ShoppingListRepository;
import com.housemate.backend.repository.user.UserRepository;
import com.housemate.shared.dto.items.request.ShoppingListCreateRequestDTO;
import com.housemate.shared.dto.items.request.ShoppingListUpdateRequestDTO;
import com.housemate.shared.dto.items.response.ShoppingListResponseDTO;
import com.housemate.shared.enums.ShoppingListStatus;
import com.housemate.shared.utils.types.ListItem;
import org.springframework.test.util.ReflectionTestUtils;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("ShoppingListService Unit Tests")
class ShoppingListServiceTest {

    // ============ Mock Dependencies ============
    @Mock
    private ShoppingListRepository shoppingListRepository;

    @Mock
    private HouseholdRepository householdRepository;

    @Mock
    private UserRepository userRepository;

    // ============ Service Under Test ============
    @InjectMocks
    private ShoppingListService shoppingListService;

    // ============ Test Data Constants ============
    private static final UUID TEST_SHOPPING_LIST_ID = UUID.fromString("00000000-0000-0000-0000-000000000101");
    private static final UUID TEST_HOUSEHOLD_ID = UUID.fromString("00000000-0000-0000-0000-000000000102");
    private static final UUID TEST_USER_ID = UUID.fromString("00000000-0000-0000-0000-000000000103");

    private static final String TEST_LIST_NAME = "Weekly Groceries";
    private static final LocalDate TEST_CREATION_DATE = LocalDate.of(2025, 1, 15);

    // ============ Test Objects ============
    private Household testHousehold;
    private User testUser;
    private ShoppingList testShoppingList;
    private List<ListItem> testListItems;

    @BeforeEach
    void setUp() {
        testHousehold = createTestHousehold();
        testUser = createTestUser();
        linkMembership(testHousehold, testUser);
        testListItems = createTestListItems();
        testShoppingList = createTestShoppingList();
    }

    // ============ Helper Methods ============

    private Household createTestHousehold() {
        Household household = new Household();
        ReflectionTestUtils.setField(household, "id", TEST_HOUSEHOLD_ID);
        household.setName("Test Household");
        return household;
    }

    private User createTestUser() {
        User user = new User("Mario", "Rossi", "mario@test.com", "password123");
        ReflectionTestUtils.setField(user, "id", TEST_USER_ID);
        return user;
    }

    private void linkMembership(Household household, User user) {
        HouseholdMembership membership = new HouseholdMembership(household, user, true);
        ReflectionTestUtils.setField(membership, "id", TEST_USER_ID);
        household.setMemberships(List.of(membership));
        user.setHouseholdMembership(membership);
    }

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

    private ShoppingList createTestShoppingList() {
        ShoppingList list = new ShoppingList(TEST_LIST_NAME, testListItems, testHousehold);
        ReflectionTestUtils.setField(list, "id", TEST_SHOPPING_LIST_ID);
        ReflectionTestUtils.setField(list, "creationDate", TEST_CREATION_DATE);
        return list;
    }

    // ============ Tests for createShoppingList ============

    @Test
    @DisplayName("createShoppingList - should create and return ShoppingListResponseDTO on valid input")
    void testCreateShoppingList_Success() {

        ShoppingListCreateRequestDTO requestDTO = new ShoppingListCreateRequestDTO(
                TEST_LIST_NAME,
                testListItems,
                TEST_HOUSEHOLD_ID,
                TEST_CREATION_DATE
        );

        when(householdRepository.findById(TEST_HOUSEHOLD_ID)).thenReturn(Optional.of(testHousehold));
        when(shoppingListRepository.save(any(ShoppingList.class))).thenAnswer(invocation -> {
            ShoppingList savedList = invocation.getArgument(0);
            ReflectionTestUtils.setField(savedList, "id", TEST_SHOPPING_LIST_ID);
            return savedList;
        });

        ShoppingListResponseDTO responseDTO = shoppingListService.createShoppingList(TEST_USER_ID, requestDTO);

        Assertions.assertNotNull(responseDTO);
        Assertions.assertNotNull(responseDTO.id());
        Assertions.assertEquals(TEST_LIST_NAME, responseDTO.name());
        Assertions.assertEquals(testListItems.size(), responseDTO.items().size());
        Assertions.assertEquals(ShoppingListStatus.NOT_STARTED, responseDTO.status());
        Assertions.assertNotNull(responseDTO.creationDate());

        verify(householdRepository).findById(TEST_HOUSEHOLD_ID);
        verify(shoppingListRepository).save(any(ShoppingList.class));
    }

    @Test
    @DisplayName("createShoppingList - should throw IllegalArgumentException when DTO is null")
    void testCreateShoppingList_DtoNull() {

        IllegalArgumentException exception = Assertions.assertThrows(IllegalArgumentException.class, () ->
                shoppingListService.createShoppingList(TEST_USER_ID, null)
        );

        Assertions.assertEquals("No request body was sent", exception.getMessage());

        verifyNoInteractions(householdRepository);
        verifyNoInteractions(shoppingListRepository);
    }

    @Test
    @DisplayName("createShoppingList - should throw IllegalArgumentException when list name is null")
    void testCreateShoppingList_NameNull() {

        ShoppingListCreateRequestDTO requestDTO = new ShoppingListCreateRequestDTO(
                null,
                testListItems,
                TEST_HOUSEHOLD_ID,
                TEST_CREATION_DATE
        );

        IllegalArgumentException exception = Assertions.assertThrows(IllegalArgumentException.class, () ->
                shoppingListService.createShoppingList(TEST_USER_ID, requestDTO)
        );

        Assertions.assertEquals("Shopping list name cannot be null", exception.getMessage());

        verifyNoInteractions(householdRepository);
        verifyNoInteractions(shoppingListRepository);
    }

    @Test
    @DisplayName("createShoppingList - should throw IllegalArgumentException when items list is null")
    void testCreateShoppingList_ItemsNull() {

        ShoppingListCreateRequestDTO requestDTO = new ShoppingListCreateRequestDTO(
                TEST_LIST_NAME,
                null,
                TEST_HOUSEHOLD_ID,
                TEST_CREATION_DATE
        );

        IllegalArgumentException exception = Assertions.assertThrows(IllegalArgumentException.class, () ->
                shoppingListService.createShoppingList(TEST_USER_ID, requestDTO)
        );

        Assertions.assertEquals("Shopping list items cannot be null", exception.getMessage());

        verifyNoInteractions(householdRepository);
        verifyNoInteractions(shoppingListRepository);
    }

    @Test
    @DisplayName("createShoppingList - should throw IllegalArgumentException when householdId is null")
    void testCreateShoppingList_HouseholdIdNull() {

        ShoppingListCreateRequestDTO requestDTO = new ShoppingListCreateRequestDTO(
                TEST_LIST_NAME,
                testListItems,
                null,
                TEST_CREATION_DATE
        );

        IllegalArgumentException exception = Assertions.assertThrows(IllegalArgumentException.class, () ->
                shoppingListService.createShoppingList(TEST_USER_ID, requestDTO)
        );

        Assertions.assertEquals("Household ID cannot be null", exception.getMessage());

        verifyNoInteractions(householdRepository);
        verifyNoInteractions(shoppingListRepository);
    }

    @Test
    @DisplayName("createShoppingList - should throw IllegalArgumentException when household not found")
    void testCreateShoppingList_HouseholdNotFound() {

        UUID nonExistingId = UUID.randomUUID();

        ShoppingListCreateRequestDTO requestDTO = new ShoppingListCreateRequestDTO(
                TEST_LIST_NAME,
                testListItems,
                nonExistingId,
                TEST_CREATION_DATE
        );

        when(householdRepository.findById(nonExistingId)).thenReturn(Optional.empty());

        IllegalArgumentException exception = Assertions.assertThrows(IllegalArgumentException.class, () ->
                shoppingListService.createShoppingList(TEST_USER_ID, requestDTO)
        );

        Assertions.assertEquals("Household with ID: " + nonExistingId + " not found.", exception.getMessage());

        verify(householdRepository).findById(nonExistingId);
        verifyNoInteractions(shoppingListRepository);
    }

    @Test
    @DisplayName("createShoppingList - should throw IllegalArgumentException when creationDate is null")
    void testCreateShoppingList_CreationDateNull() {

        ShoppingListCreateRequestDTO requestDTO = new ShoppingListCreateRequestDTO(
                TEST_LIST_NAME,
                testListItems,
                TEST_HOUSEHOLD_ID,
                null
        );

        IllegalArgumentException exception = Assertions.assertThrows(IllegalArgumentException.class, () ->
                shoppingListService.createShoppingList(TEST_USER_ID, requestDTO)
        );

        Assertions.assertEquals("Creation date cannot be null", exception.getMessage());

        verifyNoInteractions(householdRepository);
        verifyNoInteractions(shoppingListRepository);
    }

    // ============ Tests for deleteShoppingList ============

    @Test
    @DisplayName("deleteShoppingList - should delete shopping list on valid ID")
    void testDeleteShoppingList_Success() {

        when(shoppingListRepository.findById(TEST_SHOPPING_LIST_ID)).thenReturn(Optional.of(testShoppingList));

        shoppingListService.deleteShoppingList(TEST_USER_ID, TEST_SHOPPING_LIST_ID);

        verify(shoppingListRepository).findById(TEST_SHOPPING_LIST_ID);
        verify(shoppingListRepository).delete(testShoppingList);
    }

    @Test
    @DisplayName("deleteShoppingList - should throw IllegalArgumentException when listId is null")
    void testDeleteShoppingList_IdNull() {

        IllegalArgumentException exception = Assertions.assertThrows(IllegalArgumentException.class, () ->
                shoppingListService.deleteShoppingList(TEST_USER_ID, null)
        );

        Assertions.assertEquals("Shopping list ID cannot be null", exception.getMessage());

        verifyNoInteractions(shoppingListRepository);
    }

    @Test
    @DisplayName("deleteShoppingList - should throw IllegalArgumentException when shopping list not found")
    void testDeleteShoppingList_NotFound() {

        when(shoppingListRepository.findById(TEST_SHOPPING_LIST_ID)).thenReturn(Optional.empty());

        IllegalArgumentException exception = Assertions.assertThrows(IllegalArgumentException.class, () ->
                shoppingListService.deleteShoppingList(TEST_USER_ID, TEST_SHOPPING_LIST_ID)
        );

        Assertions.assertEquals("Shopping list with ID: " + TEST_SHOPPING_LIST_ID + " not found.", exception.getMessage());

        verify(shoppingListRepository).findById(TEST_SHOPPING_LIST_ID);
        verifyNoMoreInteractions(shoppingListRepository);
    }

    // ============ Tests for updateShoppingList ============

    @Test
    @DisplayName("updateShoppingList - should update items bought status on valid input")
    void testUpdateShoppingList_Success() {

        List<Boolean> boughtItems = List.of(true, false, true);
        ShoppingListUpdateRequestDTO requestDTO = new ShoppingListUpdateRequestDTO(boughtItems);

        when(shoppingListRepository.findById(TEST_SHOPPING_LIST_ID)).thenReturn(Optional.of(testShoppingList));
        when(shoppingListRepository.save(any(ShoppingList.class))).thenReturn(testShoppingList);

        ShoppingListResponseDTO responseDTO = shoppingListService.updateShoppingList(TEST_USER_ID, TEST_SHOPPING_LIST_ID, requestDTO);

        Assertions.assertNotNull(responseDTO);
        Assertions.assertEquals(TEST_SHOPPING_LIST_ID, responseDTO.id());
        Assertions.assertEquals(TEST_LIST_NAME, responseDTO.name());

        verify(shoppingListRepository).findById(TEST_SHOPPING_LIST_ID);
        verify(shoppingListRepository).save(any(ShoppingList.class));
    }

    @Test
    @DisplayName("updateShoppingList - should throw IllegalArgumentException when DTO is null")
    void testUpdateShoppingList_DtoNull() {

        IllegalArgumentException exception = Assertions.assertThrows(IllegalArgumentException.class, () ->
                shoppingListService.updateShoppingList(TEST_USER_ID, TEST_SHOPPING_LIST_ID, null)
        );

        Assertions.assertEquals("No request body was sent", exception.getMessage());

        verifyNoInteractions(shoppingListRepository);
    }

    @Test
    @DisplayName("updateShoppingList - should throw IllegalArgumentException when listId is null")
    void testUpdateShoppingList_IdNull() {

        ShoppingListUpdateRequestDTO requestDTO = new ShoppingListUpdateRequestDTO(List.of(true, false, true));

        IllegalArgumentException exception = Assertions.assertThrows(IllegalArgumentException.class, () ->
                shoppingListService.updateShoppingList(TEST_USER_ID, null, requestDTO)
        );

        Assertions.assertEquals("Shopping list ID cannot be null", exception.getMessage());

        verifyNoInteractions(shoppingListRepository);
    }

    @Test
    @DisplayName("updateShoppingList - should throw IllegalArgumentException when shopping list not found")
    void testUpdateShoppingList_NotFound() {

        ShoppingListUpdateRequestDTO requestDTO = new ShoppingListUpdateRequestDTO(List.of(true, false, true));

        when(shoppingListRepository.findById(TEST_SHOPPING_LIST_ID)).thenReturn(Optional.empty());

        IllegalArgumentException exception = Assertions.assertThrows(IllegalArgumentException.class, () ->
                shoppingListService.updateShoppingList(TEST_USER_ID, TEST_SHOPPING_LIST_ID, requestDTO)
        );

        Assertions.assertEquals("Shopping list with ID: " + TEST_SHOPPING_LIST_ID + " not found.", exception.getMessage());

        verify(shoppingListRepository).findById(TEST_SHOPPING_LIST_ID);
        verifyNoMoreInteractions(shoppingListRepository);
    }

    // ============ Tests for getShoppingListsByHousehold ============

    @Test
    @DisplayName("getShoppingListsByHousehold - should return list of ShoppingListResponseDTO on valid userId")
    void testGetShoppingListsByHousehold_Success() {

        List<ShoppingList> shoppingLists = List.of(testShoppingList);

        when(userRepository.findById(TEST_USER_ID)).thenReturn(Optional.of(testUser));
        when(shoppingListRepository.findAllByHouseholdId(TEST_HOUSEHOLD_ID)).thenReturn(shoppingLists);

        List<ShoppingListResponseDTO> responseDTO = shoppingListService.getShoppingListsByHousehold(TEST_USER_ID);

        Assertions.assertNotNull(responseDTO);
        Assertions.assertEquals(1, responseDTO.size());
        Assertions.assertEquals(TEST_LIST_NAME, responseDTO.get(0).name());

        verify(userRepository).findById(TEST_USER_ID);
        verify(shoppingListRepository).findAllByHouseholdId(TEST_HOUSEHOLD_ID);
    }

    @Test
    @DisplayName("getShoppingListsByHousehold - should return empty list when household has no shopping lists")
    void testGetShoppingListsByHousehold_EmptyList() {

        when(userRepository.findById(TEST_USER_ID)).thenReturn(Optional.of(testUser));
        when(shoppingListRepository.findAllByHouseholdId(TEST_HOUSEHOLD_ID)).thenReturn(new ArrayList<>());

        List<ShoppingListResponseDTO> responseDTO = shoppingListService.getShoppingListsByHousehold(TEST_USER_ID);

        Assertions.assertNotNull(responseDTO);
        Assertions.assertEquals(0, responseDTO.size());

        verify(userRepository).findById(TEST_USER_ID);
        verify(shoppingListRepository).findAllByHouseholdId(TEST_HOUSEHOLD_ID);
    }

    @Test
    @DisplayName("getShoppingListsByHousehold - should throw IllegalArgumentException when userId is null")
    void testGetShoppingListsByHousehold_IdNull() {

        IllegalArgumentException exception = Assertions.assertThrows(IllegalArgumentException.class, () ->
                shoppingListService.getShoppingListsByHousehold(null)
        );

        Assertions.assertEquals("Unexpectedly found the logged user to have a null id", exception.getMessage());

        verifyNoInteractions(userRepository);
        verifyNoInteractions(shoppingListRepository);
    }

    @Test
    @DisplayName("getShoppingListsByHousehold - should throw IllegalArgumentException when user not found")
    void testGetShoppingListsByHousehold_UserNotFound() {

        UUID nonExistingId = UUID.randomUUID();

        when(userRepository.findById(nonExistingId)).thenReturn(Optional.empty());

        IllegalArgumentException exception = Assertions.assertThrows(IllegalArgumentException.class, () ->
                shoppingListService.getShoppingListsByHousehold(nonExistingId)
        );

        Assertions.assertEquals("User with ID: " + nonExistingId + " not found.", exception.getMessage());

        verify(userRepository).findById(nonExistingId);
        verifyNoInteractions(shoppingListRepository);
    }
}
