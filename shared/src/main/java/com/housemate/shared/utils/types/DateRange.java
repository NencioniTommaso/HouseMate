package com.housemate.shared.utils.types;

import java.time.LocalDateTime;

public record DateRange(
    LocalDateTime startDate,
    LocalDateTime endDate
) {
    public DateRange {
        if (startDate != null && endDate != null && startDate.isAfter(endDate)) {
            throw new IllegalArgumentException("Start date must be before end date");
        }
    }
}
