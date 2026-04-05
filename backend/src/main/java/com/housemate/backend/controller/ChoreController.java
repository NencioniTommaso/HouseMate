package com.housemate.backend.controller;
import com.housemate.backend.service.ChoreService;
import com.housemate.shared.dto.chore.request.*;
import com.housemate.shared.dto.chore.response.AssignmentOverviewDTO;
import com.housemate.shared.dto.chore.response.ChoreAssignmentResponseDTO;
import com.housemate.shared.dto.chore.response.ChoreResponseDTO;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
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
    public ResponseEntity<ChoreResponseDTO> createChore(@AuthenticationPrincipal UserDetails userDetails,
                                                        @Valid @RequestBody ChoreCreateRequestDTO choreRequestDTO) {

        String userIdString = userDetails.getUsername();
        UUID userId = UUID.fromString(userIdString);

        //call service method
        ChoreResponseDTO choreResponse = choreService.createChore(userId, choreRequestDTO);

        //return as JSON response with code 201 (created)
        //the XSS risk is not present due to the answer having the specific Content-Type: application/json header
        //the description is also allowed to contain letters, numbers and spaces only, so it cannot contain any malicious code
        return ResponseEntity.status(HttpStatus.CREATED).body(choreResponse);
    }

    @DeleteMapping("/{choreId}")
    public ResponseEntity<Void> deleteChore(@AuthenticationPrincipal UserDetails userDetails, @PathVariable UUID choreId) {

        String userIdString = userDetails.getUsername();
        UUID userId = UUID.fromString(userIdString);

        choreService.deleteChore(choreId, userId);

        return ResponseEntity.noContent().build();
    }

    @PostMapping("/assignments")
    public ResponseEntity<ChoreAssignmentResponseDTO> createAssignment(@AuthenticationPrincipal UserDetails userDetails,
                                                                       @Valid @RequestBody ChoreAssignmentCreateRequestDTO requestDTO) {

        String userIdString = userDetails.getUsername();
        UUID userId = UUID.fromString(userIdString);

        ChoreAssignmentResponseDTO responseDTO = choreService.createChoreAssignment(userId, requestDTO);

        //there is no XSS risk here either: the request body contains an instance of ChoreStatus as a string
        return ResponseEntity.status(HttpStatus.CREATED).body(responseDTO);
    }

    @DeleteMapping("/assignments/{assignmentId}")
    public ResponseEntity<Void> deleteChoreAssignment(@AuthenticationPrincipal UserDetails userDetails, @PathVariable UUID assignmentId) {

        String userIdString = userDetails.getUsername();
        UUID userId = UUID.fromString(userIdString);

        choreService.deleteChoreAssignment(assignmentId, userId);

        return ResponseEntity.noContent().build();
    }

    @PatchMapping("/assignments/{assignmentId}/status")
    public ResponseEntity<Void> updateChoreStatus(@PathVariable UUID assignmentId,
                                                  @AuthenticationPrincipal UserDetails userDetails,
                                                  @Valid @RequestBody ChoreStatusUpdateRequestDTO requestDTO) {

        String userIdString = userDetails.getUsername();
        UUID userId = UUID.fromString(userIdString);

        choreService.updateChoreAssignmentStatus(assignmentId, userId, requestDTO);

        return ResponseEntity.noContent().build();
    }

    /* not implemented yet in the first version of the real application
    @PatchMapping("/assignments/{assignmentId}/reassign")
    public ResponseEntity<ChoreAssignmentResponseDTO> reassignChore(@PathVariable UUID assignmentId,
                                                                    @AuthenticationPrincipal UserDetails userDetails,
                                                                    @Valid @RequestBody ChoreReassignRequestDTO reassignRequestDTO) {

        String userIdString = userDetails.getUsername();
        UUID userId = UUID.fromString(userIdString);

        ChoreAssignmentResponseDTO modifiedChoreDTO = choreService.reassignChore(assignmentId, userId, reassignRequestDTO);

        return ResponseEntity.ok(modifiedChoreDTO);
    }
     */

    @GetMapping
    public ResponseEntity<List<ChoreResponseDTO>> getAllHouseholdChores(@AuthenticationPrincipal UserDetails userDetails) {

        String userIdString = userDetails.getUsername();
        UUID userId = UUID.fromString(userIdString);

        List<ChoreResponseDTO> responseDTOs = choreService.getAllHouseholdChores(userId);

        return ResponseEntity.ok(responseDTOs);
    }

    @GetMapping("/assignments/overview")
    public ResponseEntity<AssignmentOverviewDTO> getAssignmentOverview(@AuthenticationPrincipal UserDetails userDetails) {

        String userIdString = userDetails.getUsername();
        UUID userId = UUID.fromString(userIdString);

        AssignmentOverviewDTO overviewDTO = choreService.getAssignmentOverview(userId);

        return ResponseEntity.ok(overviewDTO);
    }

    @GetMapping("/assignments/me")
    public ResponseEntity<AssignmentOverviewDTO> getUserAssignmentOverview(@AuthenticationPrincipal UserDetails userDetails) {

        String userIdString = userDetails.getUsername();
        UUID userId = UUID.fromString(userIdString);

        AssignmentOverviewDTO overviewDTO = choreService.getUserAssignmentOverview(userId);

        return ResponseEntity.ok(overviewDTO);
    }

    @GetMapping("/assignments")
    public ResponseEntity<List<ChoreAssignmentResponseDTO>> getFilteredChoreAssignments(
            @AuthenticationPrincipal UserDetails userDetails,
            @Valid @ModelAttribute ChoreAssignmentFilterRequestDTO filterRequestDTO) {

        String userIdString = userDetails.getUsername();
        UUID userId = UUID.fromString(userIdString);

        List<ChoreAssignmentResponseDTO> responseDTOs = choreService.getFilteredChoreAssignments(
                userId, filterRequestDTO);

        return ResponseEntity.ok(responseDTOs);
    }
}
