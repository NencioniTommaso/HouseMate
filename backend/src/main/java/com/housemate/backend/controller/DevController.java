package com.housemate.backend.controller;

import com.housemate.backend.model.user.User;
import com.housemate.backend.repository.user.UserRepository;
import com.housemate.backend.service.JwtService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/dev")
public class DevController {

    //TODO: DELETE THIS ENTIRE FILE BEFORE MERGING

    private final UserRepository userRepository;
    private final JwtService jwtService;

    public DevController(UserRepository userRepository, JwtService jwtService) {
        this.userRepository = userRepository;
        this.jwtService = jwtService;
    }

    @GetMapping("/mock-login")
    public Map<String, Object> getMockLogin() {

        User testUser = userRepository.findAll().get(0);

        var mockUserDetails = org.springframework.security.core.userdetails.User.builder()
                .username(testUser.getId().toString())
                .password("")
                .authorities(java.util.Collections.emptyList())
                .build();

        String token = jwtService.generateToken(mockUserDetails);

        Map<String, Object> response = new HashMap<>();
        response.put("token", token);
        response.put("userId", testUser.getId());

        if (testUser.getHouseholdMembership() != null) {
            response.put("householdId", testUser.getHouseholdMembership().getHousehold().getId());
        }

        return response;
    }
}