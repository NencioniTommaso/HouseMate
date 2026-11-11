package com.housemate.client.service.context;

import lombok.Getter;
import lombok.Setter;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class AuthState {

    @Getter
    @Setter
    private String jwt;

    public boolean hasJwt() {
        return jwt != null && !jwt.isBlank();
    }

    public void clear() {
        this.jwt = null;
    }
}
