package com.housemate.client.service.context;

import com.housemate.shared.dto.household.response.HouseholdResponseDTO;
import com.housemate.shared.dto.user.response.UserResponseDTO;
import lombok.Getter;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.Setter;

import java.util.List;

@RequiredArgsConstructor
public class SessionManager {

    @NonNull
    @Getter
    private final AuthState authState;

    //Information about currently logged user and its household
    @Getter @Setter
    private UserResponseDTO currentUser;

    @Getter @Setter
    private HouseholdResponseDTO currentHousehold;

    //to avoid using complicated methods to retrieve it from the household
    @Getter @Setter
    private List<UserResponseDTO> currentHouseholdMembers;
}
