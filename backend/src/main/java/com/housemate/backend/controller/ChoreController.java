package com.housemate.backend.controller;
import com.housemate.backend.model.chore.Chore;
import com.housemate.backend.service.ChoreService;
import com.housemate.shared.dto.chore.request.ChoreCreateRequestDTO;
import com.housemate.shared.dto.chore.request.ChoreRequestDTO;
import com.housemate.shared.dto.chore.response.ChoreResponseDTO;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
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

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteChore(@Valid @RequestBody ChoreRequestDTO choreRequestDTO) {

        //call service method
        choreService.deleteChore(choreRequestDTO);

        return ResponseEntity.noContent().build();
    }

    @GetMapping("/{id}")
    public ResponseEntity<List<ChoreResponseDTO>> getAllHouseholdChores(@PathVariable UUID id) {

        List<ChoreResponseDTO> responseDTOs = choreService.getAllHouseholdChores(id);

        return ResponseEntity.ok(responseDTOs);
    }
}
