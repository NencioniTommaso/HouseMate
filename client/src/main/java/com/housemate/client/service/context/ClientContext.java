package com.housemate.client.service.context;

import lombok.Getter;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class ClientContext {

    @NonNull
    @Getter
    private final AuthState authState;
}
