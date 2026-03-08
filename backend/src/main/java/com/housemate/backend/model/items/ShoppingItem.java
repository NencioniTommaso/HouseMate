package com.housemate.backend.model.items;


import com.housemate.backend.model.household.Household;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.UUID;

@Entity
@Table(name = "shopping_items")
@Getter
@Setter
@NoArgsConstructor
public class ShoppingItem {

        @Id
        @Setter(AccessLevel.NONE)
        @GeneratedValue(strategy= GenerationType.UUID)
        private UUID uuid;

        @Column(name = "item_name", nullable = false, length = 100)
        private String itemName;

        //this is a string to allow "2 cans" or "100 grams" instead of just a number
        @Column(name = "quantity", nullable = false, length = 100)
        private String quantity;

        @Column(name = "is_purchased", nullable = false)
        private Boolean isPurchased;

        @ManyToOne(fetch = FetchType.LAZY)
        @JoinColumn(nullable = false, name = "belonging_household_id")
        private Household household;

        public ShoppingItem(String name, String quantity, Household household) {
            if(household == null) {
                throw new IllegalArgumentException("Household cannot be null when creating a ShoppingItem.");
            }

            if(quantity == null || quantity.trim().isEmpty()) {
                throw new IllegalArgumentException("Quantity cannot be null or empty when creating a ShoppingItem.");
            }

            if (name == null || name.trim().isEmpty()) {
                throw new IllegalArgumentException("Item name cannot be null or empty when creating a ShoppingItem.");
            }

            this.itemName = name;
            this.household = household;
            this.quantity = quantity;
            this.isPurchased = false;
        }

}
