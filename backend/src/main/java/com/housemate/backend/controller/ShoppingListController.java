package com.housemate.backend.controller;

import com.housemate.backend.service.ShoppingListService;
import com.housemate.shared.dto.items.request.ShoppingListCreateRequestDTO;
import com.housemate.shared.dto.items.request.ShoppingListUpdateRequestDTO;
import com.housemate.shared.dto.items.response.ShoppingListResponseDTO;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/shopping-lists")
public class ShoppingListController {

    private ShoppingListService shoppingListService;

    @PostMapping
    public ResponseEntity<ShoppingListResponseDTO> createShoppingList(@RequestBody ShoppingListCreateRequestDTO requestDTO) {
        ShoppingListResponseDTO responseDTO = shoppingListService.createShoppingList(requestDTO);

        return ResponseEntity.status(HttpStatus.CREATED).body(responseDTO);
    }

    @DeleteMapping("/{listId}")
    public ResponseEntity<Void> deleteShoppingList(@PathVariable UUID listId) {
        shoppingListService.deleteShoppingList(listId);

        return ResponseEntity.noContent().build();
    }

    @PatchMapping("/{itemId}")
    public ResponseEntity<ShoppingListResponseDTO> updateItemStatus(@PathVariable UUID itemId,
                                                                    @Valid @RequestBody ShoppingListUpdateRequestDTO dto) {
        ShoppingListResponseDTO responseDTO = shoppingListService.updateShoppingList(itemId, dto);

        return ResponseEntity.ok().body(responseDTO);
    }

    @GetMapping("/{householdId}")
    public ResponseEntity<List<ShoppingListResponseDTO>> getShoppingListsByHousehold(@PathVariable UUID householdId) {
        List<ShoppingListResponseDTO> responseDTO = shoppingListService.getShoppingListsByHousehold(householdId);

        return ResponseEntity.ok().body(responseDTO);
    }

}
