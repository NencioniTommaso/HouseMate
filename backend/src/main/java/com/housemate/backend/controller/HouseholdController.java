package com.housemate.backend.controller;

import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.http.ResponseEntity;
import org.springframework.lang.NonNull;
import org.springframework.web.bind.annotation.*;

import com.housemate.backend.service.HouseholdService;
import com.housemate.shared.dto.household.request.AddMemberRequestDTO;
import com.housemate.shared.dto.household.request.HouseholdCreateRequestDTO;
import com.housemate.shared.dto.household.response.HouseholdInvitationCodeResponseDTO;
import com.housemate.shared.dto.household.response.HouseholdResponseDTO;

import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;

import java.util.Objects;
import java.util.UUID;

@RestController
@RequestMapping("/api/households")
public class HouseholdController {
    private final HouseholdService householdService;

    public HouseholdController(HouseholdService householdService) {
        this.householdService = householdService;
    }

    @PostMapping
    public ResponseEntity<HouseholdResponseDTO> createHousehold(
        @NonNull @Valid @RequestBody HouseholdCreateRequestDTO requestDTO,
        @AuthenticationPrincipal UserDetails userDetails
    ) {
        String userIdString = userDetails.getUsername();
        UUID userId = Objects.requireNonNull(
            UUID.fromString(userIdString),
            "Unexpectedly null user ID in UserDetails principal"
        );

        HouseholdResponseDTO response = householdService.createHousehold(userId, requestDTO);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping("/me")
    public ResponseEntity<HouseholdResponseDTO> getCurrentUserHousehold(
            @AuthenticationPrincipal UserDetails userDetails
    ) {
        String userIdString = userDetails.getUsername();
        UUID userId = Objects.requireNonNull(
            UUID.fromString(userIdString),
            "Unexpectedly null user ID in UserDetails principal"
        );

        HouseholdResponseDTO response = householdService.getCurrentUserHousehold(userId);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/members")
    public ResponseEntity<HouseholdResponseDTO> addMember(
            @NonNull @Valid @RequestBody AddMemberRequestDTO requestDTO,
            @AuthenticationPrincipal UserDetails userDetails
    ) {
        String userIdString = userDetails.getUsername();
        UUID userId = Objects.requireNonNull(
            UUID.fromString(userIdString),
            "Unexpectedly null user ID in UserDetails principal"
        );

        HouseholdResponseDTO response = householdService.addMember(userId, requestDTO);
        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/members/{memberId}")
    public ResponseEntity<HouseholdResponseDTO> removeMember(
            @NonNull @PathVariable UUID memberId,
            @AuthenticationPrincipal UserDetails userDetails
    ) {
        String userIdString = userDetails.getUsername();
        UUID userId = Objects.requireNonNull(
            UUID.fromString(userIdString),
            "Unexpectedly null user ID in UserDetails principal"
        );

        HouseholdResponseDTO response = householdService.removeMember(userId, memberId);
        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/me")
    public ResponseEntity<Void> leaveHousehold(@AuthenticationPrincipal UserDetails userDetails) {
        String userIdString = userDetails.getUsername();
        UUID userId = Objects.requireNonNull(
            UUID.fromString(userIdString),
            "Unexpectedly null user ID in UserDetails principal"
        );

        householdService.leaveHousehold(userId);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/invitation-code")
    public ResponseEntity<HouseholdInvitationCodeResponseDTO> getInvitationCode(
            @AuthenticationPrincipal UserDetails userDetails
    ) {
        String userIdString = userDetails.getUsername();
        UUID userId = Objects.requireNonNull(
            UUID.fromString(userIdString),
            "Unexpectedly null user ID in UserDetails principal"
        );

        HouseholdInvitationCodeResponseDTO response = householdService.getInvitationCode(userId);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/invitation-code/refresh")
    public ResponseEntity<HouseholdInvitationCodeResponseDTO> refreshInvitationCode(
            @AuthenticationPrincipal UserDetails userDetails
    ) {
        String userIdString = userDetails.getUsername();
        UUID userId = Objects.requireNonNull(
            UUID.fromString(userIdString),
            "Unexpectedly null user ID in UserDetails principal"
        );

        HouseholdInvitationCodeResponseDTO response = householdService.refreshInvitationCode(userId);
        return ResponseEntity.ok(response);
    }
}
