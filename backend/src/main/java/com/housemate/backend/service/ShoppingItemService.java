package com.housemate.backend.service;

import com.housemate.backend.model.household.Household;
import com.housemate.backend.model.items.ShoppingItem;
import com.housemate.backend.repository.household.HouseholdRepository;
import com.housemate.backend.repository.items.ShoppingItemRepository;
import com.housemate.shared.dto.items.request.ShoppingItemCreateRequestDTO;
import com.housemate.shared.dto.items.request.ShoppingItemQuantityUpdateRequestDTO;
import com.housemate.shared.dto.items.request.ShoppingItemStatusUpdateRequestDTO;
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

    @Transactional
    public ShoppingItemResponseDTO updateItemQuantity(UUID itemId, ShoppingItemQuantityUpdateRequestDTO requestDTO) {

        log.info("Received request to update shopping item quantity. ItemId: {}, New Quantity: {}", itemId, requestDTO.quantity());

        ShoppingItem itemToUpdate = shoppingItemRepository.findById(itemId)
                .orElseThrow(() -> new IllegalArgumentException("Shopping item not found with id: " + itemId));

        itemToUpdate.setQuantity(requestDTO.quantity());
        ShoppingItem updatedItem = shoppingItemRepository.save(itemToUpdate);

        log.info("ShoppingItem quantity updated. Id: {}, New Quantity: {}", updatedItem.getUuid(), updatedItem.getQuantity());

        return new ShoppingItemResponseDTO(
                updatedItem.getUuid(),
                updatedItem.getItemName(),
                updatedItem.getQuantity(),
                updatedItem.getIsPurchased(),
                updatedItem.getHousehold().getId()
        );
    }

    @Transactional
    public ShoppingItemResponseDTO updateItemStatus(UUID itemId, ShoppingItemStatusUpdateRequestDTO requestDTO) {

        log.info("Received request to update shopping item status. ItemId: {}, New Status: {}", itemId, requestDTO.isBought());

        ShoppingItem itemToUpdate = shoppingItemRepository.findById(itemId)
                .orElseThrow(() -> new IllegalArgumentException("Shopping item not found with id: " + itemId));

        itemToUpdate.setIsPurchased(requestDTO.isBought());
        ShoppingItem updatedItem = shoppingItemRepository.save(itemToUpdate);

        log.info("ShoppingItem status updated. Id: {}, New Status: {}", updatedItem.getUuid(), updatedItem.getIsPurchased());

        return new ShoppingItemResponseDTO(
                updatedItem.getUuid(),
                updatedItem.getItemName(),
                updatedItem.getQuantity(),
                updatedItem.getIsPurchased(),
                updatedItem.getHousehold().getId()
        );
    }

    @Transactional
    public ShoppingItemResponseDTO getShoppingItemById(UUID itemId) {

        log.info("Received request to get shopping item by id: {}", itemId);

        ShoppingItem item = shoppingItemRepository.findById(itemId)
                .orElseThrow(() -> new IllegalArgumentException("Shopping item not found with id: " + itemId));

        log.info("ShoppingItem retrieved. Id: {}", item.getUuid());

        return new ShoppingItemResponseDTO(
                item.getUuid(),
                item.getItemName(),
                item.getQuantity(),
                item.getIsPurchased(),
                item.getHousehold().getId()
        );

    }
}
