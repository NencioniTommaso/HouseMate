package com.housemate.shared.dto.user;

import java.util.UUID;

public record UserDTO(UUID id, String name, String surname, String email, String iban) {}
