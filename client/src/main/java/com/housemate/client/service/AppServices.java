package com.housemate.client.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.housemate.client.service.context.ClientContext;
import com.housemate.shared.dto.household.response.HouseholdResponseDTO;
import com.housemate.shared.dto.user.response.UserResponseDTO;
import lombok.Getter;
import lombok.Setter;

import java.net.http.HttpClient;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

public class AppServices {

    private ClientContext clientContext;

    @Getter
    private final ChoreClientService choreClientService;
    @Getter
    private final ShoppingListClientService  shoppingListClientService;

    public AppServices(HttpClient httpClient, ObjectMapper objectMapper, ClientContext clientContext) {
        this.clientContext = clientContext;
        this.clientContext.getAuthState().setJwt("eyJhbGciOiJIUzM4NCJ9.eyJzdWIiOiIzOWIyNDg3My0wZmFiLTQ4MDEtYmM3Zi1kZDZhNzQ4MzAzZjMiLCJpYXQiOjE3NzQzNDMyMTAsImV4cCI6MTc3NDQyOTYxMH0.vq-9O4xgtIkWaxGR2E3okgEAeGSi9TEBV937yM-SAJD1RhCLpzrbFieVfQx2W6bi");
        HttpRestClient httpRestClient = new HttpRestClient(httpClient, clientContext, objectMapper);
        this.choreClientService = new ChoreClientService(httpRestClient);
        this.shoppingListClientService = new ShoppingListClientService(httpRestClient);

        this.currentUser = new UserResponseDTO(
                UUID.fromString("398bc183-6408-4a81-be4d-e1251dbf597f"),
                "Test",
                "Surname",
                null,
                null
        );

        this.currentHousehold = new HouseholdResponseDTO(
                UUID.fromString("f832af3b-9b7d-407f-a8ec-6d359f0ef3d9"),
                "Test Household",
                LocalDate.now(),
                List.of(currentUser)
        );
    }

    //Information about currently logged user and its household
    @Getter @Setter
    private UserResponseDTO currentUser;

    @Getter @Setter
    private HouseholdResponseDTO currentHousehold;
}
