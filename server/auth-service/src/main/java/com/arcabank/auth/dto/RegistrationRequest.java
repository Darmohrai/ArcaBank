package com.arcabank.auth.dto;

public record RegistrationRequest(
    String passport_id,
    String email,
    String firstName,
    String lastName,
    String phoneNumber,
    String password
) {}
