package com.housemate.shared.dto.items.response;

import java.util.UUID;

public record ShoppingItemResponseDTO(

        UUID id,
        String name,
        String quantity,
        Boolean isBought,
        UUID householdId
) {}
