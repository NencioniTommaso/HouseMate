package com.housemate.shared.dto.auth;

public record RegisterRequestDTO(String name, String surname, String email, String password, String iban) {}
