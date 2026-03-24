package com.housemate.backend.config;

import com.housemate.backend.model.household.Household;
import com.housemate.backend.model.household.HouseholdMembership;
import com.housemate.backend.model.user.User;
import com.housemate.backend.repository.household.HouseholdMembershipRepository;
import com.housemate.backend.repository.household.HouseholdRepository;
import com.housemate.backend.repository.user.UserRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.transaction.support.TransactionTemplate;

@Configuration
public class TestDataInitializer {

    @Bean
    CommandLineRunner initDatabase(HouseholdRepository householdRepository,
                                   UserRepository userRepository,
                                   HouseholdMembershipRepository householdMembershipRepository,
                                   TransactionTemplate transactionTemplate) {
        return args -> {

            User finalTestUser = transactionTemplate.execute(status -> {

                Household household;
                if (householdRepository.count() == 0) {
                    household = new Household();
                    household.setName("Casa di Test (Generata)");
                    household = householdRepository.save(household);
                } else {
                    household = householdRepository.findAll().get(0);
                }

                User user;
                if (userRepository.count() == 0) {
                    user = new User();
                    user.setName("Utente di Test (Generato)");
                    user.setSurname("t");
                    user.setEmail("test@email.com");
                    user.setPassword("123456");
                    user = userRepository.save(user);
                } else {
                    user = userRepository.findAll().get(0);
                }

                if (householdMembershipRepository.count() == 0) {
                    HouseholdMembership membership = new HouseholdMembership(household, user, true);
                    householdMembershipRepository.save(membership);
                }

                return user;
            });

            Household finalHousehold = householdRepository.findAll().get(0);


            System.out.println("=========================================================");
            System.out.println("👤 USER ID: " + finalTestUser.getId());
            System.out.println("🏠 HOUSEHOLD ID: " + finalHousehold.getId());
            System.out.println("=========================================================");

        };
    }
}