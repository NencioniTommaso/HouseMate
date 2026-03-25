package com.housemate.backend.model.items;


import com.housemate.backend.model.household.Household;
import com.housemate.shared.enums.ShoppingListStatus;
import com.housemate.shared.utils.types.ListItem;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.JdbcTypeCode;

import java.util.List;
import java.util.UUID;

@Entity
@Table(name = "shopping_lists")
@Getter
@Setter
@NoArgsConstructor
public class ShoppingList {

        @Id
        @Setter(AccessLevel.NONE)
        @GeneratedValue(strategy= GenerationType.UUID)
        private UUID id;

        @Column(name = "list_name", nullable = false, length = 100)
        private String listName;

        @Column(name = "items", nullable = false, length = 100)
        @JdbcTypeCode(org.hibernate.type.SqlTypes.JSON)
        private List<ListItem> listItems;

        @Column(name = "list_status", nullable = false)
        private ShoppingListStatus listStatus;

        @ManyToOne(fetch = FetchType.LAZY)
        @JoinColumn(nullable = false, name = "belonging_household_id")
        private Household household;

        public ShoppingList(String name, List<com.housemate.shared.utils.types.ListItem> items, Household household) {
            if(household == null) {
                throw new IllegalArgumentException("Household cannot be null when creating a ShoppingList.");
            }

            if(items == null) {
                throw new IllegalArgumentException("Item list cannot be null when creating a ShoppingList.");
            }

            if (name == null || name.trim().isEmpty()) {
                throw new IllegalArgumentException("List name cannot be null or empty when creating a ShoppingList.");
            }

            this.listName = name;
            this.household = household;
            this.listItems = items;
            this.listStatus = ShoppingListStatus.NOT_STARTED;
        }

}
