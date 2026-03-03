package com.housemate.backend.config;

import com.housemate.backend.model.household.Household;
import com.housemate.backend.repository.household.HouseholdRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class TestDataInitializer {

    @Bean
    CommandLineRunner initDatabase(HouseholdRepository householdRepository) {
        return args -> {
            // Controlla se il database è vuoto per evitare di creare doppioni ad ogni riavvio
            if (householdRepository.count() == 0) {

                // Crea una casa "fantasma"
                Household testHousehold = new Household();
                testHousehold.setName("Casa di Test (Generata)");

                // La salva nel DB
                Household saved = householdRepository.save(testHousehold);

                // Stampa l'UUID in console con dei bordi vistosi per fartelo notare!
                System.out.println("\n==========================================================");
                System.out.println("CASA DI TEST CREATA AUTOMATICAMENTE!");
                System.out.println("Usa questo householdId per i tuoi test su Swagger:");
                System.out.println(saved.getId());
                System.out.println("==========================================================\n");
            }
        };
    }
}