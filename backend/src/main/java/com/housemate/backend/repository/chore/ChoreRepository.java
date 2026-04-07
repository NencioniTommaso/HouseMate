package com.housemate.backend.repository.chore;


import com.housemate.backend.model.chore.Chore;
import org.springframework.data.domain.Limit;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface ChoreRepository extends JpaRepository<Chore, UUID> {

    Chore findByDescriptionAndHouseholdId(String description, UUID householdId);

    List<Chore> findAllByHouseholdId(UUID householdId);
}
