package com.housemate.backend.controller;

import com.housemate.backend.service.ShoppingItemService;
import com.housemate.shared.dto.items.request.ShoppingItemCreateRequestDTO;
import com.housemate.shared.dto.items.request.ShoppingItemQuantityUpdateRequestDTO;
import com.housemate.shared.dto.items.request.ShoppingItemStatusUpdateRequestDTO;
import com.housemate.shared.dto.items.response.ShoppingItemResponseDTO;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/shopping-items")
public class ShoppingItemController {

    private ShoppingItemService shoppingItemService;

    @PostMapping
    public ResponseEntity<ShoppingItemResponseDTO> createShoppingItem(@RequestBody ShoppingItemCreateRequestDTO requestDTO) {
        ShoppingItemResponseDTO responseDTO = shoppingItemService.createShoppingItem(requestDTO);

        return ResponseEntity.status(HttpStatus.CREATED).body(responseDTO);
    }

    @DeleteMapping("/{itemId}")
    public ResponseEntity<Void> deleteShoppingItem(@PathVariable UUID itemId) {
        shoppingItemService.deleteShoppingItem(itemId);

        return ResponseEntity.noContent().build();
    }

    @PatchMapping("/{itemId}/quantity")
    public ResponseEntity<ShoppingItemResponseDTO> updateItemQuantity(@PathVariable UUID itemId, @RequestBody ShoppingItemQuantityUpdateRequestDTO dto) {
        ShoppingItemResponseDTO responseDTO = shoppingItemService.updateItemQuantity(itemId, dto);

        return ResponseEntity.ok().body(responseDTO);
    }

    @PatchMapping("/{itemId}/status")
    public ResponseEntity<ShoppingItemResponseDTO> updateItemStatus(@PathVariable UUID itemId, @RequestBody ShoppingItemStatusUpdateRequestDTO dto) {
        ShoppingItemResponseDTO responseDTO = shoppingItemService.updateItemStatus(itemId, dto);

        return ResponseEntity.ok().body(responseDTO);
    }

    @GetMapping("/{itemId}")
    public ResponseEntity<ShoppingItemResponseDTO> getShoppingItemById(@PathVariable UUID itemId) {
        ShoppingItemResponseDTO responseDTO = shoppingItemService.getShoppingItemById(itemId);

        return ResponseEntity.ok().body(responseDTO);
    }

    @GetMapping("/household/{householdId}")
    public ResponseEntity<List<ShoppingItemResponseDTO>> getShoppingItemsByHousehold(
            @PathVariable UUID householdId,
            @RequestParam(required = false) Boolean isBought) {
        List<ShoppingItemResponseDTO> responseDTO = shoppingItemService.getShoppingItemsByHousehold(householdId, isBought);

        return ResponseEntity.ok().body(responseDTO);
    }

}
