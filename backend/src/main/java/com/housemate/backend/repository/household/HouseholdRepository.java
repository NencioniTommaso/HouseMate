package com.housemate.backend.repository.household;

import com.housemate.backend.model.household.Household;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface HouseholdRepository extends JpaRepository<Household, UUID> {

    Optional<Household> findByName(String name);

    Optional<Household> findByInvitationCode(String invitationCode);

    Optional<Household> findByMemberships_User_Id(UUID userId);

    boolean existsByName(String name);

    boolean existsByInvitationCode(String invitationCode);

}