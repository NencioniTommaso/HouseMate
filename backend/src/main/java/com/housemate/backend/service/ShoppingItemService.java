package com.housemate.backend.service;

import com.housemate.backend.model.household.Household;
import com.housemate.backend.model.items.ShoppingItem;
import com.housemate.backend.repository.household.HouseholdRepository;
import com.housemate.backend.repository.items.ShoppingItemRepository;
import com.housemate.shared.dto.items.request.ShoppingItemCreateRequestDTO;
import com.housemate.shared.dto.items.response.ShoppingItemResponseDTO;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class ShoppingItemService {


    private HouseholdRepository householdRepository;
    private ShoppingItemRepository shoppingItemRepository;

    public ShoppingItemResponseDTO createShoppingItem(ShoppingItemCreateRequestDTO requestDTO) {

        Household household = householdRepository.findById(requestDTO.householdId())
                .orElseThrow(() -> new IllegalArgumentException("Household not found with id: " + requestDTO.householdId()));

        ShoppingItem shoppingItem = new ShoppingItem();
        shoppingItem.setItemName(requestDTO.name());
        shoppingItem.setQuantity(requestDTO.quantity());
        shoppingItem.setHousehold(household);

        ShoppingItem savedItem = shoppingItemRepository.save(shoppingItem);

        return new ShoppingItemResponseDTO(
                savedItem.getUuid(),
                savedItem.getItemName(),
                savedItem.getQuantity(),
                savedItem.getIsPurchased(),
                savedItem.getHousehold().getId()
        );

    }
}
