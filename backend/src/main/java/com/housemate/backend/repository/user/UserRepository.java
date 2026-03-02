package com.housemate.backend.repository.user;

import com.housemate.backend.model.user.User;
import com.housemate.backend.model.household.HouseholdMembership;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface UserRepository extends JpaRepository<User, UUID> {
    List<User> findByHouseholdMembership(HouseholdMembership householdMembership);
    boolean existsByHouseholdMembership(HouseholdMembership householdMembership);

    Optional<User> findByEmail(String email);
    boolean existsByEmail(String email);
}


