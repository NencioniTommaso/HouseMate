package com.housemate.backend.service;

import com.housemate.backend.model.household.Household;
import com.housemate.backend.model.items.ShoppingList;
import com.housemate.backend.model.user.User;
import com.housemate.backend.repository.household.HouseholdRepository;
import com.housemate.backend.repository.items.ShoppingListRepository;
import com.housemate.backend.repository.user.UserRepository;
import com.housemate.shared.dto.items.request.ShoppingListCreateRequestDTO;
import com.housemate.shared.dto.items.request.ShoppingListUpdateRequestDTO;
import com.housemate.shared.dto.items.response.ShoppingListResponseDTO;
import com.housemate.shared.enums.ShoppingListStatus;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.lang.NonNull;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.Assert;

import java.util.List;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class ShoppingListService {

    private final ShoppingListRepository shoppingListRepository;
    private final HouseholdRepository householdRepository;
    private final UserRepository userRepository;

    @Transactional
    public ShoppingListResponseDTO createShoppingList(@NonNull UUID userId,
                                                      @NonNull ShoppingListCreateRequestDTO requestDTO) {

        Assert.notNull(userId, "Unexpectedly found the logged user to have a null id");

        Assert.notNull(requestDTO, "No request body was sent");

        Assert.notNull(requestDTO.name(), "Shopping list name cannot be null");
        Assert.notNull(requestDTO.items(), "Shopping list items cannot be null");
        Assert.notNull(requestDTO.householdId(), "Household ID cannot be null");
        Assert.notNull(requestDTO.creationDate(), "Creation date cannot be null");

        log.info("Received request to create shopping list: {}", requestDTO);

        Household household = householdRepository.findById(requestDTO.householdId())
                .orElseThrow(() -> new IllegalArgumentException("Household with ID: " + requestDTO.householdId() + " not found."));

        checkIfHouseholdMember(userId, household);

        ShoppingList shoppingList = new ShoppingList(requestDTO.name(), requestDTO.items(), household);

        ShoppingList savedItem = shoppingListRepository.save(shoppingList);

        log.info("ShoppingList created. Id: {}", savedItem.getId());

        return new ShoppingListResponseDTO(
                savedItem.getId(),
                savedItem.getListName(),
                savedItem.getListItems(),
                savedItem.getListStatus(),
                savedItem.getHousehold().getId(),
                savedItem.getCreationDate()
        );
    }

    @Transactional
    public void deleteShoppingList(@NonNull UUID userId, @NonNull UUID listId) {

        Assert.notNull(userId, "Unexpectedly found the logged user to have a null id");

        Assert.notNull(listId, "Shopping list ID cannot be null");

        log.info("Received request to delete shopping list: {}", listId);

        ShoppingList listToDelete = shoppingListRepository.findById(listId)
                .orElseThrow(() -> new IllegalArgumentException("Shopping list with ID: " + listId + " not found."));

        checkIfHouseholdMember(userId, listToDelete.getHousehold());

        shoppingListRepository.delete(listToDelete);

        log.info("ShoppingList deleted. Id: {}", listToDelete.getId());
    }

    @Transactional
    public ShoppingListResponseDTO updateShoppingList(@NonNull UUID userId,
                                                      @NonNull UUID listId,
                                                      @NonNull ShoppingListUpdateRequestDTO requestDTO) {

        Assert.notNull(userId, "Unexpectedly found the logged user to have a null id");

        Assert.notNull(requestDTO, "No request body was sent");
        Assert.notNull(listId, "Shopping list ID cannot be null");
        Assert.notNull(requestDTO.boughtItems(), "Items bought list cannot be null");

        log.info("Received request to update shopping list: {}", listId);

        ShoppingList listToUpdate = shoppingListRepository.findById(listId)
                .orElseThrow(() -> new IllegalArgumentException("Shopping list with ID: " + listId + " not found."));

        checkIfHouseholdMember(userId, listToUpdate.getHousehold());

        for (int i = 0; i < listToUpdate.getListItems().size(); i++) {
            listToUpdate.getListItems().get(i).setBought(requestDTO.boughtItems().get(i));
        }

        //item updates can only go from "not bought" to "bought" and never the opposite
        if (listToUpdate.getListItems().stream().anyMatch(item -> !item.isBought())) {
            listToUpdate.setListStatus(ShoppingListStatus.IN_PROGRESS);
        } else {
            listToUpdate.setListStatus(ShoppingListStatus.COMPLETED);
        }

        shoppingListRepository.save(listToUpdate);

        log.info("ShoppingList updated. Id: {}", listToUpdate.getId());

        return new ShoppingListResponseDTO(
                listToUpdate.getId(),
                listToUpdate.getListName(),
                listToUpdate.getListItems(),
                listToUpdate.getListStatus(),
                listToUpdate.getHousehold().getId(),
                listToUpdate.getCreationDate()
        );
    }

    @Transactional
    public List<ShoppingListResponseDTO> getShoppingListsByHousehold(@NonNull UUID userId) {

        Assert.notNull(userId, "Unexpectedly found the logged user to have a null id");

        log.info("Received request to get shopping lists for user {}'s household: ", userId);

        Household household = getCurrentHousehold(userId);
        checkIfHouseholdMember(userId, household);

        List<ShoppingList> lists = shoppingListRepository.findAllByHouseholdId(household.getId());

        log.info("Retrieved {} shopping lists for household: {}", lists.size(), household.getId());

        return lists.stream()
                .map(list -> new ShoppingListResponseDTO(
                        list.getId(),
                        list.getListName(),
                        list.getListItems(),
                        list.getListStatus(),
                        list.getHousehold().getId(),
                        list.getCreationDate()
                ))
                .toList();
    }

    private void checkIfHouseholdMember(UUID userId, Household household) {

        List<UUID> membersIds = household.getMemberships().stream()
                .map(membership -> membership.getUser().getId()).toList();

        if(!membersIds.contains(userId)){
            throw new AccessDeniedException("The logged user is not a member of household " + household.getName());
        }
    }

    private Household getCurrentHousehold(@NonNull UUID userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("User with ID: " + userId + " not found."));

        if(user.getHouseholdMembership() == null || user.getHouseholdMembership().getHousehold() == null) {
            throw new IllegalStateException("User with ID: " + userId + " is not currently a member of any household.");
        }

        return user.getHouseholdMembership().getHousehold();
    }
}

