package com.housemate.backend.repository.items;

import com.housemate.backend.model.items.ShoppingItem;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;


@Repository
public interface ShoppingItemRepository extends JpaRepository<ShoppingItem, UUID> {

    public List<ShoppingItem> findByHouseholdId(UUID householdId);


}
