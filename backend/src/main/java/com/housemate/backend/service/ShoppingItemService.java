package com.housemate.backend.service;

import com.housemate.backend.model.household.Household;
import com.housemate.backend.model.items.ShoppingItem;
import com.housemate.backend.repository.household.HouseholdRepository;
import com.housemate.backend.repository.items.ShoppingItemRepository;
import com.housemate.shared.dto.items.request.ShoppingItemCreateRequestDTO;
import com.housemate.shared.dto.items.response.ShoppingItemResponseDTO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class ShoppingItemService {


    private HouseholdRepository householdRepository;
    private ShoppingItemRepository shoppingItemRepository;

    @Transactional
    public ShoppingItemResponseDTO createShoppingItem(ShoppingItemCreateRequestDTO requestDTO) {

        log.info("Received request to create shopping item: {}", requestDTO);

        Household household = householdRepository.findById(requestDTO.householdId())
                .orElseThrow(() -> new IllegalArgumentException("Household not found with id: " + requestDTO.householdId()));

        ShoppingItem shoppingItem = new ShoppingItem();
        shoppingItem.setItemName(requestDTO.name());
        shoppingItem.setQuantity(requestDTO.quantity());
        shoppingItem.setHousehold(household);

        ShoppingItem savedItem = shoppingItemRepository.save(shoppingItem);

        log.info("ShoppingItem created. Id: {}", savedItem.getUuid());

        return new ShoppingItemResponseDTO(
                savedItem.getUuid(),
                savedItem.getItemName(),
                savedItem.getQuantity(),
                savedItem.getIsPurchased(),
                savedItem.getHousehold().getId()
        );
    }

    @Transactional
    public void deleteShoppingItem(UUID itemId) {

        log.info("Received request to delete shopping item: {}", itemId);

        ShoppingItem itemToDelete = shoppingItemRepository.findById(itemId)
                .orElseThrow(() -> new IllegalArgumentException("Shopping item not found with id: " + itemId));

        shoppingItemRepository.delete(itemToDelete);

        log.info("ShoppingItem deleted. Id: {}", itemToDelete.getUuid());
    }


}
