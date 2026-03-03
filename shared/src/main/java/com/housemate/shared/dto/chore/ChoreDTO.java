package com.housemate.shared.dto.chore;

import java.util.UUID;

public record ChoreDTO(UUID id, String description, int frequencyDays) {}
