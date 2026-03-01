package com.housemate.backend.repository.household;

import com.housemate.backend.model.household.HouseholdMembership;
import com.housemate.backend.model.household.Household;
import com.housemate.backend.model.user.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface HouseholdMembershipRepository extends JpaRepository<HouseholdMembership, UUID> {

    List<HouseholdMembership> findByHousehold(Household household);
    List<HouseholdMembership> findByHouseholdAndIsAdminTrue(Household household);
    List<HouseholdMembership> findByUser(User user);
    Optional<HouseholdMembership> findByHouseholdAndUser(Household household, User user);
}