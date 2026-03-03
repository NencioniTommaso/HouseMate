package com.housemate.backend.controller;
import com.housemate.backend.service.ChoreService;
import com.housemate.shared.dto.chore.request.ChoreCreateRequestDTO;
import com.housemate.shared.dto.chore.response.ChoreResponseDTO;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/chores")
public class ChoreController {

    private final ChoreService choreService;

    public ChoreController(ChoreService choreService) {
        this.choreService = choreService;
    }

    @PostMapping
    public ChoreResponseDTO createChore(ChoreCreateRequestDTO choreCreateRequestDTO) {

        return choreService.createChore(choreCreateRequestDTO);
    }

    @PutMapping("/{id}")
    public String updateChore(@PathVariable UUID id) {
        return "Update chore with ID: " + id;
    }

    @DeleteMapping("/{id}")
    public String deleteChore(@PathVariable UUID id) {
        return "Delete chore with ID: " + id;
    }
}
