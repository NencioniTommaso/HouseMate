package com.housemate.backend.config;

import com.housemate.backend.model.household.Household;
import com.housemate.backend.model.user.User;
import com.housemate.backend.repository.household.HouseholdRepository;
import com.housemate.backend.repository.user.UserRepository;
import com.housemate.backend.service.JwtService;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.core.userdetails.UserDetails;


@Configuration
public class TestDataInitializer {

    @Bean
    CommandLineRunner initDatabase(HouseholdRepository householdRepository, UserRepository userRepository, JwtService jwtService) {
        return args -> {

            User testUser =  new User();
            Household testHousehold = new  Household();
            if (householdRepository.count() == 0) {

                testHousehold.setName("Casa di Test (Generata)");

                testHousehold = householdRepository.save(testHousehold);

            }

            if (userRepository.count() == 0) {

                testUser = new User("name", "surname", "email", "password");
                testUser.setName("Utente di Test (Generato)");

                testUser = userRepository.save(testUser);

            }


            UserDetails mockUserDetails = org.springframework.security.core.userdetails.User.builder()
                    .username(testUser.getId().toString()) // Mettiamo l'UUID nel campo username
                    .password("password_finta") // La password non serve per il JWT, ma non può essere null
                    .roles("USER") // Diamo un ruolo base
                    .build();

            String devToken = jwtService.generateToken(mockUserDetails); // o come si chiama il metodo del tuo collega

            System.out.println("=========================================================");
            System.out.println("🔑 DEV JWT TOKEN PER IL FRONTEND: " + devToken);
            System.out.println("👤 USER ID: " + testUser.getId());
            System.out.println("🏠 HOUSEHOLD ID: " + testHousehold.getId());
            System.out.println("=========================================================");

        };
    }
}