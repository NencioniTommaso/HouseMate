package com.housemate.backend.controller;

import com.housemate.backend.service.ShoppingListService;
import com.housemate.shared.dto.items.request.ShoppingListCreateRequestDTO;
import com.housemate.shared.dto.items.request.ShoppingListUpdateRequestDTO;
import com.housemate.shared.dto.items.response.ShoppingListResponseDTO;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/shopping-lists")
public class ShoppingListController {

    private final ShoppingListService shoppingListService;

    public ShoppingListController(ShoppingListService shoppingListService) {
        this.shoppingListService = shoppingListService;
    }

    @PostMapping
    public ResponseEntity<ShoppingListResponseDTO> createShoppingList(
            @AuthenticationPrincipal UserDetails userDetails,
            @Valid @RequestBody ShoppingListCreateRequestDTO requestDTO) {

        String userIdString = userDetails.getUsername();
        UUID userId = UUID.fromString(userIdString);

        ShoppingListResponseDTO responseDTO = shoppingListService.createShoppingList(userId, requestDTO);

        return ResponseEntity.status(HttpStatus.CREATED).body(responseDTO);
    }

    @DeleteMapping("/{listId}")
    public ResponseEntity<Void> deleteShoppingList(@AuthenticationPrincipal UserDetails userDetails,
                                                   @PathVariable UUID listId) {

        String userIdString = userDetails.getUsername();
        UUID userId = UUID.fromString(userIdString);

        shoppingListService.deleteShoppingList(userId, listId);

        return ResponseEntity.noContent().build();
    }

    @PatchMapping("/{listId}")
    public ResponseEntity<ShoppingListResponseDTO> updateListStatus(@AuthenticationPrincipal UserDetails userDetails,
                                                                    @PathVariable UUID listId,
                                                                    @Valid @RequestBody ShoppingListUpdateRequestDTO dto) {

        String userIdString = userDetails.getUsername();
        UUID userId = UUID.fromString(userIdString);

        ShoppingListResponseDTO responseDTO = shoppingListService.updateShoppingList(userId, listId, dto);

        return ResponseEntity.ok().body(responseDTO);
    }

    @GetMapping()
    public ResponseEntity<List<ShoppingListResponseDTO>> getShoppingListsByHousehold(
            @AuthenticationPrincipal UserDetails userDetails) {

        String userIdString = userDetails.getUsername();
        UUID userId = UUID.fromString(userIdString);

        List<ShoppingListResponseDTO> responseDTO = shoppingListService.getShoppingListsByHousehold(userId);

        return ResponseEntity.ok().body(responseDTO);
    }

}
