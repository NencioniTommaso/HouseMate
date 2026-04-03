package com.housemate.backend.controller;

import com.housemate.backend.service.UserService;
import com.housemate.shared.dto.user.request.UserUpdateRequestDTO;
import com.housemate.shared.dto.user.response.UserResponseDTO;
import jakarta.validation.Valid;
import org.springframework.lang.NonNull;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.Objects;
import java.util.UUID;

@RestController
@RequestMapping("/api/users")
public class UserController {

    private final UserService userService;

    public UserController(UserService userService) {
        this.userService = userService;
    }

    @GetMapping("/me")
    public ResponseEntity<UserResponseDTO> getCurrentUser(@AuthenticationPrincipal UserDetails userDetails) {
        UUID userId = Objects.requireNonNull(
            UUID.fromString(userDetails.getUsername()),
            "Unexpectedly null user ID in UserDetails principal"
        );

        UserResponseDTO response = userService.getCurrentUser(userId);
        return ResponseEntity.ok(response);
    }

        @PatchMapping("/me")
    public ResponseEntity<UserResponseDTO> updateCurrentUser(
            @NonNull @Valid @RequestBody UserUpdateRequestDTO requestDTO,
            @AuthenticationPrincipal UserDetails userDetails
    ) {
        UUID userId = Objects.requireNonNull(
            UUID.fromString(userDetails.getUsername()),
            "Unexpectedly null user ID in UserDetails principal"
        );

        UserResponseDTO response = userService.updateCurrentUser(userId, requestDTO);
        return ResponseEntity.ok(response);
    }
}
