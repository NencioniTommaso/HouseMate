package com.housemate.client.service;

import com.housemate.shared.dto.household.response.HouseholdResponseDTO;
import com.housemate.shared.dto.user.response.UserResponseDTO;
import lombok.Getter;
import lombok.Setter;

public class AppServices {

    //Client services (to communicate with the backend)
    /*
    private final UserClientService userClientService = new UserClientService();
    private final HouseholdClientService householdClientService = new HouseholdClientService();
    private final ChoreClientService choreClientService = new ChoreClientService();
    private final ExpenseClientService expenseClientService = new ExpenseClientService();
    private final DebtClientService debtClientService = new DebtClientService();
    private final SettlementClientService settlementClientService = new SettlementClientService();
    */

    //Information about currently logged user and its household
    @Getter @Setter
    private UserResponseDTO currentUser;

    @Getter @Setter
    private HouseholdResponseDTO currentHousehold;


}
