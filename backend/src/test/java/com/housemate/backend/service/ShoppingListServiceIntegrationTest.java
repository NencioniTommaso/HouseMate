package com.housemate.backend.service;

import com.housemate.backend.model.items.ShoppingList;
import com.housemate.backend.model.user.User;
import com.housemate.backend.repository.household.HouseholdRepository;
import com.housemate.backend.repository.items.ShoppingListRepository;
import com.housemate.backend.repository.user.UserRepository;
import com.housemate.shared.dto.household.request.AddMemberRequestDTO;
import com.housemate.shared.dto.household.request.HouseholdCreateRequestDTO;
import com.housemate.shared.dto.household.response.HouseholdInvitationCodeResponseDTO;
import com.housemate.shared.dto.household.response.HouseholdResponseDTO;
import com.housemate.shared.dto.items.request.ShoppingListCreateRequestDTO;
import com.housemate.shared.dto.items.request.ShoppingListUpdateRequestDTO;
import com.housemate.shared.dto.items.response.ShoppingListResponseDTO;
import com.housemate.shared.enums.ShoppingListStatus;
import com.housemate.shared.utils.types.ListItem;
import jakarta.persistence.EntityManager;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@SpringBootTest
@ActiveProfiles("test")
@Transactional
@DisplayName("ShoppingListService Integration Tests")
@SuppressWarnings("null")
class ShoppingListServiceIntegrationTest {

    @Autowired
    private ShoppingListService shoppingListService;

    @Autowired
    private HouseholdService householdService;

    @Autowired
    private ShoppingListRepository shoppingListRepository;

    @Autowired
    private HouseholdRepository householdRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private EntityManager entityManager;

    private User user1;
    private User user2;
    private User user3;
    private UUID primaryHouseholdId;
    private UUID isolatedHouseholdId;

    @BeforeEach
    void setUp() {
        // Seed Primary Household with User 1 (Admin) and User 2 (Member)
        user1 = persistUser("Alice", "Admin", "alice.shopping@test.com");
        user2 = persistUser("Bob", "Member", "bob.shopping@test.com");

        HouseholdResponseDTO primaryHousehold = householdService.createHousehold(
                user1.getId(),
                new HouseholdCreateRequestDTO("Primary Household")
        );
        primaryHouseholdId = primaryHousehold.id();

        HouseholdInvitationCodeResponseDTO codeResponse = householdService.getInvitationCode(user1.getId());
        householdService.addMember(user2.getId(), new AddMemberRequestDTO(codeResponse.invitationCode()));

        // Seed Isolated Household with User 3 (Admin) for scoping tests
        user3 = persistUser("Charlie", "Outsider", "charlie.shopping@test.com");
        HouseholdResponseDTO isolatedHousehold = householdService.createHousehold(
                user3.getId(),
                new HouseholdCreateRequestDTO("Isolated Household")
        );
        isolatedHouseholdId = isolatedHousehold.id();

        entityManager.flush();
        entityManager.clear();
    }

    @Test
    @DisplayName("Scenario 1: Create a shopping list with initial ListItem entries -> verify persistence and returned DTO")
    void shouldCreateShoppingListWithInitialItemsAndPersistSuccessfully() {
        List<ListItem> initialItems = new ArrayList<>();
        initialItems.add(new ListItem("Oat Milk"));
        initialItems.add(new ListItem("Sourdough Bread"));
        initialItems.add(new ListItem("Free Range Eggs"));

        ShoppingListCreateRequestDTO createRequest = new ShoppingListCreateRequestDTO(
                "Weekly Groceries",
                initialItems,
                primaryHouseholdId,
                LocalDate.now()
        );

        ShoppingListResponseDTO response = shoppingListService.createShoppingList(user1.getId(), createRequest);

        assertThat(response).isNotNull();
        assertThat(response.id()).isNotNull();
        assertThat(response.name()).isEqualTo("Weekly Groceries");
        assertThat(response.status()).isEqualTo(ShoppingListStatus.NOT_STARTED);
        assertThat(response.householdId()).isEqualTo(primaryHouseholdId);
        assertThat(response.creationDate()).isEqualTo(LocalDate.now());
        assertThat(response.items()).hasSize(3);
        assertThat(response.items()).extracting(ListItem::getItemName)
                .containsExactly("Oat Milk", "Sourdough Bread", "Free Range Eggs");
        assertThat(response.items()).extracting(ListItem::isBought)
                .containsOnly(false);

        // Verify database persistence after clearing cache
        entityManager.flush();
        entityManager.clear();

        Optional<ShoppingList> persistedOptional = shoppingListRepository.findById(response.id());
        assertThat(persistedOptional).isPresent();

        ShoppingList persisted = persistedOptional.get();
        assertThat(persisted.getListName()).isEqualTo("Weekly Groceries");
        assertThat(persisted.getListStatus()).isEqualTo(ShoppingListStatus.NOT_STARTED);
        assertThat(persisted.getHousehold().getId()).isEqualTo(primaryHouseholdId);
        assertThat(persisted.getListItems()).hasSize(3);
        assertThat(persisted.getListItems()).extracting(ListItem::getItemName)
                .containsExactly("Oat Milk", "Sourdough Bread", "Free Range Eggs");
        assertThat(persisted.getListItems()).extracting(ListItem::isBought)
                .containsOnly(false);

        // Verify User 2 (household member) can also create a list in the same household
        List<ListItem> memberItems = List.of(new ListItem("Coffee Beans"));
        ShoppingListCreateRequestDTO memberRequest = new ShoppingListCreateRequestDTO(
                "Coffee Supplies",
                memberItems,
                primaryHouseholdId,
                LocalDate.now()
        );
        ShoppingListResponseDTO memberResponse = shoppingListService.createShoppingList(user2.getId(), memberRequest);
        assertThat(memberResponse).isNotNull();
        assertThat(memberResponse.id()).isNotNull();
        assertThat(memberResponse.name()).isEqualTo("Coffee Supplies");
    }

    @Test
    @DisplayName("Scenario 2: Update shopping list items and transition ShoppingListStatus across lifecycle")
    void shouldUpdateShoppingListItemsAndTransitionStatuses() {
        List<ListItem> initialItems = new ArrayList<>();
        initialItems.add(new ListItem("Apples"));
        initialItems.add(new ListItem("Bananas"));
        initialItems.add(new ListItem("Carrots"));

        ShoppingListCreateRequestDTO createRequest = new ShoppingListCreateRequestDTO(
                "Fruit and Veg",
                initialItems,
                primaryHouseholdId,
                LocalDate.now()
        );

        ShoppingListResponseDTO createdList = shoppingListService.createShoppingList(user1.getId(), createRequest);
        UUID listId = createdList.id();
        assertThat(createdList.status()).isEqualTo(ShoppingListStatus.NOT_STARTED);

        entityManager.flush();
        entityManager.clear();

        // 1. Partial update: 1 item bought -> status should transition to IN_PROGRESS
        ShoppingListUpdateRequestDTO partialUpdate = new ShoppingListUpdateRequestDTO(List.of(true, false, false));
        ShoppingListResponseDTO partialResponse = shoppingListService.updateShoppingList(user1.getId(), listId, partialUpdate);

        assertThat(partialResponse.status()).isEqualTo(ShoppingListStatus.IN_PROGRESS);
        assertThat(partialResponse.items().get(0).isBought()).isTrue();
        assertThat(partialResponse.items().get(1).isBought()).isFalse();
        assertThat(partialResponse.items().get(2).isBought()).isFalse();

        entityManager.flush();
        entityManager.clear();

        ShoppingList reloadedPartial = shoppingListRepository.findById(listId).orElseThrow();
        assertThat(reloadedPartial.getListStatus()).isEqualTo(ShoppingListStatus.IN_PROGRESS);
        assertThat(reloadedPartial.getListItems().get(0).isBought()).isTrue();
        assertThat(reloadedPartial.getListItems().get(1).isBought()).isFalse();
        assertThat(reloadedPartial.getListItems().get(2).isBought()).isFalse();

        // 2. Full update: All items bought -> status should transition to COMPLETED (updated by User 2)
        ShoppingListUpdateRequestDTO completeUpdate = new ShoppingListUpdateRequestDTO(List.of(true, true, true));
        ShoppingListResponseDTO completeResponse = shoppingListService.updateShoppingList(user2.getId(), listId, completeUpdate);

        assertThat(completeResponse.status()).isEqualTo(ShoppingListStatus.COMPLETED);
        assertThat(completeResponse.items()).extracting(ListItem::isBought).containsOnly(true);

        entityManager.flush();
        entityManager.clear();

        ShoppingList reloadedComplete = shoppingListRepository.findById(listId).orElseThrow();
        assertThat(reloadedComplete.getListStatus()).isEqualTo(ShoppingListStatus.COMPLETED);
        assertThat(reloadedComplete.getListItems()).extracting(ListItem::isBought).containsOnly(true);

        // 3. Reset update: All items unbought -> status should revert to NOT_STARTED
        ShoppingListUpdateRequestDTO resetUpdate = new ShoppingListUpdateRequestDTO(List.of(false, false, false));
        ShoppingListResponseDTO resetResponse = shoppingListService.updateShoppingList(user1.getId(), listId, resetUpdate);

        assertThat(resetResponse.status()).isEqualTo(ShoppingListStatus.NOT_STARTED);
        assertThat(resetResponse.items()).extracting(ListItem::isBought).containsOnly(false);

        entityManager.flush();
        entityManager.clear();

        ShoppingList reloadedReset = shoppingListRepository.findById(listId).orElseThrow();
        assertThat(reloadedReset.getListStatus()).isEqualTo(ShoppingListStatus.NOT_STARTED);
        assertThat(reloadedReset.getListItems()).extracting(ListItem::isBought).containsOnly(false);
    }

    @Test
    @DisplayName("Scenario 3: Retrieve household shopping lists and verify item persistence and scoping")
    void shouldRetrieveHouseholdShoppingListsWithItemPersistenceAndScoping() {
        // Create 2 shopping lists in Primary Household
        ShoppingListCreateRequestDTO list1Request = new ShoppingListCreateRequestDTO(
                "Pantry Staples",
                List.of(new ListItem("Rice"), new ListItem("Pasta")),
                primaryHouseholdId,
                LocalDate.now()
        );
        ShoppingListCreateRequestDTO list2Request = new ShoppingListCreateRequestDTO(
                "Produce",
                List.of(new ListItem("Tomatoes"), new ListItem("Lettuce"), new ListItem("Cucumbers")),
                primaryHouseholdId,
                LocalDate.now()
        );

        shoppingListService.createShoppingList(user1.getId(), list1Request);
        shoppingListService.createShoppingList(user2.getId(), list2Request);

        // Create 1 shopping list in Isolated Household by User 3
        ShoppingListCreateRequestDTO isolatedRequest = new ShoppingListCreateRequestDTO(
                "Isolated Groceries",
                List.of(new ListItem("Tea"), new ListItem("Sugar")),
                isolatedHouseholdId,
                LocalDate.now()
        );
        shoppingListService.createShoppingList(user3.getId(), isolatedRequest);

        entityManager.flush();
        entityManager.clear();

        // Retrieve lists for User 1 -> should only contain Primary Household lists (2 items)
        List<ShoppingListResponseDTO> user1Lists = shoppingListService.getShoppingListsByHousehold(user1.getId());
        assertThat(user1Lists).hasSize(2);
        assertThat(user1Lists).extracting(ShoppingListResponseDTO::name)
                .containsExactlyInAnyOrder("Pantry Staples", "Produce");
        assertThat(user1Lists).allMatch(dto -> dto.householdId().equals(primaryHouseholdId));

        // Verify items of "Pantry Staples"
        ShoppingListResponseDTO pantryList = user1Lists.stream()
                .filter(l -> l.name().equals("Pantry Staples"))
                .findFirst().orElseThrow();
        assertThat(pantryList.items()).extracting(ListItem::getItemName)
                .containsExactly("Rice", "Pasta");

        // Retrieve lists for User 2 (member of Primary Household) -> should see the same lists
        List<ShoppingListResponseDTO> user2Lists = shoppingListService.getShoppingListsByHousehold(user2.getId());
        assertThat(user2Lists).hasSize(2);
        assertThat(user2Lists).extracting(ShoppingListResponseDTO::name)
                .containsExactlyInAnyOrder("Pantry Staples", "Produce");

        // Retrieve lists for User 3 (Isolated Household) -> should only see isolated list
        List<ShoppingListResponseDTO> user3Lists = shoppingListService.getShoppingListsByHousehold(user3.getId());
        assertThat(user3Lists).hasSize(1);
        assertThat(user3Lists.get(0).name()).isEqualTo("Isolated Groceries");
        assertThat(user3Lists.get(0).householdId()).isEqualTo(isolatedHouseholdId);
    }

    @Test
    @DisplayName("Scenario 4: Delete shopping list and verify household scoping prevents outsider access")
    void shouldDeleteShoppingListAndEnforceHouseholdScoping() {
        ShoppingListCreateRequestDTO createRequest = new ShoppingListCreateRequestDTO(
                "Party Supplies",
                List.of(new ListItem("Cups"), new ListItem("Napkins"), new ListItem("Snacks")),
                primaryHouseholdId,
                LocalDate.now()
        );

        ShoppingListResponseDTO createdList = shoppingListService.createShoppingList(user1.getId(), createRequest);
        UUID listId = createdList.id();

        entityManager.flush();
        entityManager.clear();

        // 1. Outsider (User 3) cannot create a shopping list targeting Primary Household
        ShoppingListCreateRequestDTO outsiderCreateRequest = new ShoppingListCreateRequestDTO(
                "Intruder List",
                List.of(new ListItem("Spyware")),
                primaryHouseholdId,
                LocalDate.now()
        );
        assertThatThrownBy(() -> shoppingListService.createShoppingList(user3.getId(), outsiderCreateRequest))
                .isInstanceOf(AccessDeniedException.class)
                .hasMessageContaining("The logged user is not a member of household");

        // 2. Outsider (User 3) cannot update Primary Household's shopping list
        ShoppingListUpdateRequestDTO outsiderUpdateRequest = new ShoppingListUpdateRequestDTO(List.of(true, true, true));
        assertThatThrownBy(() -> shoppingListService.updateShoppingList(user3.getId(), listId, outsiderUpdateRequest))
                .isInstanceOf(AccessDeniedException.class)
                .hasMessageContaining("The logged user is not a member of household");

        // 3. Outsider (User 3) cannot delete Primary Household's shopping list
        assertThatThrownBy(() -> shoppingListService.deleteShoppingList(user3.getId(), listId))
                .isInstanceOf(AccessDeniedException.class)
                .hasMessageContaining("The logged user is not a member of household");

        // Verify list still exists after unauthorized attempts
        assertThat(shoppingListRepository.findById(listId)).isPresent();

        // 4. Legitimate member (User 2) deletes the shopping list successfully
        shoppingListService.deleteShoppingList(user2.getId(), listId);

        entityManager.flush();
        entityManager.clear();

        // Verify list is deleted from database
        assertThat(shoppingListRepository.findById(listId)).isEmpty();

        // Subsequent delete attempt fails with IllegalArgumentException
        assertThatThrownBy(() -> shoppingListService.deleteShoppingList(user1.getId(), listId))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("not found");
    }

    @Test
    @DisplayName("Edge Case: User without household cannot retrieve shopping lists")
    void shouldFailWhenUserWithoutHouseholdAttemptsRetrieval() {
        User homelessUser = persistUser("Dave", "Homeless", "dave.homeless@test.com");

        entityManager.flush();
        entityManager.clear();

        assertThatThrownBy(() -> shoppingListService.getShoppingListsByHousehold(homelessUser.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("is not currently a member of any household");
    }

    private User persistUser(String name, String surname, String email) {
        User user = new User(name, surname, email, "encodedPassword123!");
        return userRepository.saveAndFlush(user);
    }
}
