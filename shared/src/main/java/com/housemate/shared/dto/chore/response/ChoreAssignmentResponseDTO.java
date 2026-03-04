package com.housemate.shared.dto.chore.response;

import com.housemate.shared.enums.ChoreStatus;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

/**
 * DTO representing a chore assignment to be displayed on the client.
 */
public record ChoreAssignmentResponseDTO(
    UUID assignmentId,
    String choreDescription,
    String assignedUserName,
    LocalDateTime dueDate,
    ChoreStatus status
) {}
