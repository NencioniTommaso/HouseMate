package com.housemate.backend;

import com.housemate.shared.dto.common.StatusDTO;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import java.time.LocalDateTime;

@RestController
public class StatusController {

    @GetMapping("/api/status")
    public StatusDTO checkStatus() {
        System.out.println("LOG SERVER: Ho ricevuto una richiesta dal Client!");
        return new StatusDTO("Il Server HouseMate è ONLINE!", LocalDateTime.now().toString());

    }
}