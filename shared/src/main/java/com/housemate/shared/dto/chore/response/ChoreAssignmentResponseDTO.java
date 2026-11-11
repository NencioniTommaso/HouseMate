package com.housemate.shared.dto.chore.response;

import com.housemate.shared.dto.user.response.UserResponseDTO;
import com.housemate.shared.enums.ChoreStatus;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * DTO representing a chore assignment to be displayed on the client.
 */
public record ChoreAssignmentResponseDTO(
    UUID assignmentId,
    UUID choreId,
    String choreDescription,
    UserResponseDTO assignedUser,
    LocalDateTime dueDate,
    ChoreStatus status
) {}
