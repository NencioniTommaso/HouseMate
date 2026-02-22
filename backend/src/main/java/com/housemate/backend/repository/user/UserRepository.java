package com.housemate.backend.repository.user;

import com.housemate.backend.model.user.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository // Segnala a Spring che questa classe gestisce l'accesso ai dati
public interface UserRepository extends JpaRepository<User, UUID> {

    // 1. Trova un utente dalla sua email (Utile per il Login!)
    Optional<User> findByEmail(String email);

    // 2. Controlla se un'email è già registrata (Utile per la Registrazione!)
    boolean existsByEmail(String email);

}


