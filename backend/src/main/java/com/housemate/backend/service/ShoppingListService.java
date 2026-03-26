package com.housemate.backend.service;

import com.housemate.backend.model.household.Household;
import com.housemate.backend.model.items.ShoppingList;
import com.housemate.backend.repository.household.HouseholdRepository;
import com.housemate.backend.repository.items.ShoppingListRepository;
import com.housemate.shared.dto.items.request.ShoppingListCreateRequestDTO;
import com.housemate.shared.dto.items.request.ShoppingListUpdateRequestDTO;
import com.housemate.shared.dto.items.response.ShoppingListResponseDTO;
import com.housemate.shared.enums.ShoppingListStatus;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.lang.NonNull;
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

    @Transactional
    public ShoppingListResponseDTO createShoppingList(@NonNull ShoppingListCreateRequestDTO requestDTO) {

        Assert.notNull(requestDTO, "No request body was sent");

        Assert.notNull(requestDTO.name(), "Shopping list name cannot be null");
        Assert.notNull(requestDTO.items(), "Shopping list items cannot be null");
        Assert.notNull(requestDTO.householdId(), "Household ID cannot be null");
        Assert.notNull(requestDTO.creationDate(), "Creation date cannot be null");

        log.info("Received request to create shopping list: {}", requestDTO);

        Household household = householdRepository.findById(requestDTO.householdId())
                .orElseThrow(() -> new IllegalArgumentException("Household with ID: " + requestDTO.householdId() + " not found."));

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
    public void deleteShoppingList(@NonNull UUID listId) {

        Assert.notNull(listId, "Shopping list ID cannot be null");

        log.info("Received request to delete shopping list: {}", listId);

        ShoppingList listToDelete = shoppingListRepository.findById(listId)
                .orElseThrow(() -> new IllegalArgumentException("Shopping list with ID: " + listId + " not found."));

        shoppingListRepository.delete(listToDelete);

        log.info("ShoppingList deleted. Id: {}", listToDelete.getId());
    }

    @Transactional
    public ShoppingListResponseDTO updateShoppingList(@NonNull UUID listId, @NonNull ShoppingListUpdateRequestDTO requestDTO) {

        Assert.notNull(requestDTO, "No request body was sent");
        Assert.notNull(listId, "Shopping list ID cannot be null");
        Assert.notNull(requestDTO.boughtItems(), "Items bought list cannot be null");

        log.info("Received request to update shopping list: {}", listId);

        ShoppingList listToUpdate = shoppingListRepository.findById(listId)
                .orElseThrow(() -> new IllegalArgumentException("Shopping list with ID: " + listId + " not found."));

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
    public List<ShoppingListResponseDTO> getShoppingListsByHousehold(@NonNull UUID householdId) {

        Assert.notNull(householdId, "Household ID cannot be null");

        log.info("Received request to get shopping lists for household: {}", householdId);

        householdRepository.findById(householdId)
                .orElseThrow(() -> new IllegalArgumentException("Household with ID: " + householdId + " not found."));

        List<ShoppingList> lists = shoppingListRepository.findAllByHouseholdId(householdId);

        log.info("Retrieved {} shopping lists for household: {}", lists.size(), householdId);

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
}

