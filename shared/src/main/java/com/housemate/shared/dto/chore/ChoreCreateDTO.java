package com.housemate.shared.dto.chore;

import java.util.UUID;

public record ChoreCreateDTO(String description, int frequencyDays, UUID householdId) {}
