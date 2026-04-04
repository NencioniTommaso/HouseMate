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

    @Getter //this is the same instance that the httpRestClient uses to store the jwt
    private final ClientContext clientContext;
    @Getter
    private final AuthClientService authClientService;
    @Getter
    private final UserClientService userClientService;
    @Getter
    private final HouseholdClientService householdClientService;
    @Getter
    private final ChoreClientService choreClientService;
    @Getter
    private final ShoppingListClientService  shoppingListClientService;
    @Getter
    private final ExpenseClientService expenseClientService;
    @Getter
    private final SettlementClientService settlementClientService;
    @Getter
    private final DebtClientService debtClientService;

    public AppServices(HttpClient httpClient, ObjectMapper objectMapper, ClientContext clientContext) {
        this.clientContext = clientContext;
        HttpRestClient httpRestClient = new HttpRestClient(httpClient, clientContext, objectMapper);
        this.authClientService = new AuthClientService(httpRestClient);
        this.userClientService = new UserClientService(httpRestClient);
        this.householdClientService = new HouseholdClientService(httpRestClient);
        this.choreClientService = new ChoreClientService(httpRestClient);
        this.shoppingListClientService = new ShoppingListClientService(httpRestClient);
        this.expenseClientService = new ExpenseClientService(httpRestClient);
        this.settlementClientService = new SettlementClientService(httpRestClient);
        this.debtClientService = new DebtClientService(httpRestClient);
    }

    //Information about currently logged user and its household
    @Getter @Setter
    private UserResponseDTO currentUser;

    @Getter @Setter
    private HouseholdResponseDTO currentHousehold;
}
