package com.housemate.backend.repository.user;

import com.housemate.backend.model.user.Unavailability;
import com.housemate.backend.model.user.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface UnavailabilityRepository extends JpaRepository<Unavailability, UUID> {
    List<Unavailability> findByUser(User user);
}


