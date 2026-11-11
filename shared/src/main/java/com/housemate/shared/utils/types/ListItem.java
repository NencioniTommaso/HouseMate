package com.housemate.shared.utils.types;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.Setter;

public class ListItem {

    @JsonCreator
    public ListItem(@JsonProperty("itemName") String itemName) {
        this.itemName = itemName;
    }

    @Getter
    private final String itemName;

    @Getter @Setter
    private boolean isBought;
}
