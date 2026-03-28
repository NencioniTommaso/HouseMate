package com.housemate.shared.dto.auth.response;

import com.housemate.shared.dto.user.response.UserResponseDTO;

/**
 * DTO returned after successful authentication/registration.
 */
public record LoginResponseDTO(
    UserResponseDTO user,
    String token
) {}
