package com.housemate.backend.controller;
import com.housemate.backend.service.ChoreService;
import com.housemate.shared.dto.chore.request.*;
import com.housemate.shared.dto.chore.response.AssignmentOverviewDTO;
import com.housemate.shared.dto.chore.response.ChoreAssignmentResponseDTO;
import com.housemate.shared.dto.chore.response.ChoreResponseDTO;
import com.housemate.shared.enums.ChoreStatus;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/chores")
public class ChoreController {

    private final ChoreService choreService;

    public ChoreController(ChoreService choreService) {
        this.choreService = choreService;
    }

    @PostMapping
    public ResponseEntity<ChoreResponseDTO> createChore(@Valid @RequestBody ChoreCreateRequestDTO choreRequestDTO) {

        //call service method
        ChoreResponseDTO choreResponse = choreService.createChore(choreRequestDTO);

        //return as JSON response with code 201 (created)
        //the XSS risk is not present due to the answer having the specific Content-Type: application/json header
        //the description is also allowed to contain letters, numbers and spaces only, so it cannot contain any malicious code
        return ResponseEntity.status(HttpStatus.CREATED).body(choreResponse);
    }

    @DeleteMapping("/{choreId}")
    public ResponseEntity<Void> deleteChore(@PathVariable UUID choreId) {

        //call service method
        choreService.deleteChore(choreId);

        return ResponseEntity.noContent().build();
    }

    @PostMapping("/assignments")
    public ResponseEntity<ChoreAssignmentResponseDTO> createAssignment(@Valid @RequestBody ChoreAssignmentCreateRequestDTO requestDTO) {

        ChoreAssignmentResponseDTO responseDTO = choreService.createChoreAssignment(requestDTO);

        //there is no XSS risk here either: the request body contains an instance of ChoreStatus as a string
        return ResponseEntity.status(HttpStatus.CREATED).body(responseDTO);
    }

    @DeleteMapping("/assigments/{assignmentId}")
    public ResponseEntity<Void> deleteChoreAssignment(@PathVariable UUID assignmentId) {
        choreService.deleteChoreAssignment(assignmentId);

        return ResponseEntity.noContent().build();
    }

    @PatchMapping("/assignments/{assignmentId}/status")
    public ResponseEntity<Void> updateChoreStatus(@PathVariable UUID id,
                                                  @Valid @RequestBody ChoreStatusUpdateRequestDTO requestDTO) {

        choreService.updateChoreAssignmentStatus(id, requestDTO);

        return ResponseEntity.noContent().build();
    }

    @PatchMapping("/assignments/{assignmentId}/reassign")
    public ResponseEntity<ChoreAssignmentResponseDTO> reassignChore(@PathVariable UUID assignmentId,
                                                                    @Valid @RequestBody ChoreReassignRequestDTO reassignRequestDTO) {

        ChoreAssignmentResponseDTO modifiedChoreDTO = choreService.reassignChore(assignmentId, reassignRequestDTO.newAssigneeId());

        return ResponseEntity.ok(modifiedChoreDTO);
    }

    @GetMapping("/{householdId}")
    public ResponseEntity<List<ChoreResponseDTO>> getAllHouseholdChores(@PathVariable UUID householdId) {

        List<ChoreResponseDTO> responseDTOs = choreService.getAllHouseholdChores(householdId);

        return ResponseEntity.ok(responseDTOs);
    }

    @GetMapping("/assignments/{householdId}/overview")
    public ResponseEntity<AssignmentOverviewDTO> getAssignmentOverview(@PathVariable UUID householdId) {

        AssignmentOverviewDTO overviewDTO = choreService.getAssignmentOverview(householdId);

        return ResponseEntity.ok(overviewDTO);
    }

    @GetMapping("/assignments")
    public ResponseEntity<List<ChoreAssignmentResponseDTO>> getFilteredChoreAssignments(
            @AuthenticationPrincipal UserDetails userDetails,
            @Valid @ModelAttribute ChoreAssignmentFilterRequestDTO filterRequestDTO) {

        String userIdString = userDetails.getUsername();
        UUID userId = UUID.fromString(userIdString);

        List<ChoreAssignmentResponseDTO> responseDTOs = choreService.getFilteredChoreAssignments(userId, filterRequestDTO);

        return ResponseEntity.ok(responseDTOs);
    }


}
