package com.housemate.shared.dto.user.response;

import java.util.UUID;

/**
 * DTO representing a persisted User to be displayed on the client.
 */
public record UserResponseDTO(
    UUID id,
    String name,
    String surname,
    String email,
    String iban
) {}
