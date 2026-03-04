package com.housemate.shared.dto.chore.request;

import java.util.UUID;

public record ChoreReassignRequestDTO(
        UUID newAssigneeId
) {}
