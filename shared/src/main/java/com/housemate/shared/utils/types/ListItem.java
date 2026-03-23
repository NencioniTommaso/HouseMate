package com.housemate.shared.utils.types;

import lombok.Getter;
import lombok.Setter;

public class ListItem {

    public ListItem(String itemName) {
        this.itemName = itemName;
    }

    @Getter
    private final String itemName;

    @Getter @Setter
    private boolean isBought;
}
