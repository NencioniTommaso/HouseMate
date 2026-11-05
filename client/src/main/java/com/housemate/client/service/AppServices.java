package com.housemate.client.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.housemate.client.service.context.SessionManager;
import com.housemate.shared.dto.household.response.HouseholdResponseDTO;
import com.housemate.shared.dto.user.response.UserResponseDTO;
import lombok.Getter;
import lombok.Setter;

import java.net.http.HttpClient;
import java.util.List;

public class AppServices {

    @Getter //this is the same instance that the httpRestClient uses to store the jwt
    private final SessionManager sessionManager;
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

    public AppServices(HttpClient httpClient, ObjectMapper objectMapper, SessionManager sessionManager) {
        this.sessionManager = sessionManager;
        HttpRestClient httpRestClient = new HttpRestClient(httpClient, sessionManager, objectMapper);
        this.authClientService = new AuthClientService(httpRestClient);
        this.userClientService = new UserClientService(httpRestClient);
        this.householdClientService = new HouseholdClientService(httpRestClient);
        this.choreClientService = new ChoreClientService(httpRestClient);
        this.shoppingListClientService = new ShoppingListClientService(httpRestClient);
        this.expenseClientService = new ExpenseClientService(httpRestClient);
        this.settlementClientService = new SettlementClientService(httpRestClient);
        this.debtClientService = new DebtClientService(httpRestClient);
    }
}
