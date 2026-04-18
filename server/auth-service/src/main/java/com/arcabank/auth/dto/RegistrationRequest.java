package com.arcabank.auth.dto;

public record RegistrationRequest(
    String username,
    String email,
    String firstName,
    String lastName,
    String password
) {}
