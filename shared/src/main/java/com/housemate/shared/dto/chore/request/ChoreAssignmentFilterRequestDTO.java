package com.housemate.shared.dto.chore.request;

import com.housemate.shared.enums.ChoreStatus;
import com.housemate.shared.utils.types.DateRange;
import jakarta.validation.constraints.NotNull;

import java.util.List;
import java.util.UUID;

public record ChoreAssignmentFilterRequestDTO(
        List<ChoreStatus> statuses,
        UUID assigneeId,
        String descriptionContains,

        @NotNull(message = "Date range cannot be null")
        DateRange dateRange
) {}
