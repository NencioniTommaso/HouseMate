package com.housemate.backend.controller;

import com.housemate.backend.service.ShoppingItemService;
import com.housemate.shared.dto.items.request.ShoppingItemCreateRequestDTO;
import com.housemate.shared.dto.items.response.ShoppingItemResponseDTO;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

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




}
