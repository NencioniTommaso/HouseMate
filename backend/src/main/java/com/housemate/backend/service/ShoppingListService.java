package com.housemate.backend.service;

import com.housemate.backend.model.household.Household;
import com.housemate.backend.model.items.ShoppingList;
import com.housemate.backend.repository.household.HouseholdRepository;
import com.housemate.backend.repository.items.ShoppingListRepository;
import com.housemate.shared.dto.items.request.ShoppingListCreateRequestDTO;
import com.housemate.shared.dto.items.request.ShoppingListUpdateRequestDTO;
import com.housemate.shared.dto.items.response.ShoppingListResponseDTO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class ShoppingListService {

    private HouseholdRepository householdRepository;
    private ShoppingListRepository shoppingListRepository;

    @Transactional
    public ShoppingListResponseDTO createShoppingList(ShoppingListCreateRequestDTO requestDTO) {

        log.info("Received request to create shopping list: {}", requestDTO);

        Household household = householdRepository.findById(requestDTO.householdId())
                .orElseThrow(() -> new IllegalArgumentException("Household not found with id: " + requestDTO.householdId()));

        ShoppingList shoppingList = new ShoppingList();

        ShoppingList savedItem = shoppingListRepository.save(shoppingList);

        log.info("ShoppingItem created. Id: {}", savedItem.getId());

        return new ShoppingListResponseDTO(
                savedItem.getId(),
                savedItem.getListName(),
                savedItem.getListItems(),
                savedItem.getListStatus(),
                savedItem.getHousehold().getId()
        );
    }

    @Transactional
    public void deleteShoppingList(UUID listId) {

        log.info("Received request to delete shopping list: {}", listId);

        ShoppingList listToDelete = shoppingListRepository.findById(listId)
                .orElseThrow(() -> new IllegalArgumentException("Shopping item not found with id: " + listId));

        shoppingListRepository.delete(listToDelete);

        log.info("ShoppingItem deleted. Id: {}", listToDelete.getId());
    }

    @Transactional
    public ShoppingListResponseDTO updateShoppingList(UUID listId, ShoppingListUpdateRequestDTO requestDTO) {

        log.info("Received request to update shopping list: {}", listId);

        ShoppingList listToUpdate = shoppingListRepository.findById(listId)
                .orElseThrow(() -> new IllegalArgumentException("Shopping item not found with id: " + listId));

        for (int i = 0; i < listToUpdate.getListItems().size(); i++) {
            listToUpdate.getListItems().get(i).setBought(requestDTO.boughtItems().get(i));
        }

        shoppingListRepository.save(listToUpdate);

        log.info("ShoppingItem updated. Id: {}", listToUpdate.getId());

        return new ShoppingListResponseDTO(
                listToUpdate.getId(),
                listToUpdate.getListName(),
                listToUpdate.getListItems(),
                listToUpdate.getListStatus(),
                listToUpdate.getHousehold().getId()
        );
    }

    @Transactional
    public List<ShoppingListResponseDTO> getShoppingListsByHousehold(UUID householdId) {

        log.info("Received request to get shopping lists for household: {}", householdId);

        householdRepository.findById(householdId)
                .orElseThrow(() -> new IllegalArgumentException("Household not found with id: " + householdId));

        List<ShoppingList> lists = shoppingListRepository.findAllByHouseholdId(householdId);

        log.info("Retrieved {} shopping lists for household: {}", lists.size(), householdId);

        return lists.stream()
                .map(list -> new ShoppingListResponseDTO(
                        list.getId(),
                        list.getListName(),
                        list.getListItems(),
                        list.getListStatus(),
                        list.getHousehold().getId()
                ))
                .toList();
    }
}
