package com.housemate.shared.utils.types;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;

@RequiredArgsConstructor
public class ListItem {

    @Getter
    private String itemName;

    @Getter @Setter
    private boolean isBought;
}
