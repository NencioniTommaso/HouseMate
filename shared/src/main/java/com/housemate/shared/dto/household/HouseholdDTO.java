package com.housemate.shared.dto.household;

import com.housemate.shared.dto.user.UserDTO;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

public record HouseholdDTO(UUID id, String name, LocalDate creationDate, List<UserDTO> members) {}
