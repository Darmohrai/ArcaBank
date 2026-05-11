package com.arcabank.auth.dto;

import java.util.UUID;

public record UserResponse(
    UUID id,
    String firstName,
    String lastName,
    String email,
    String passportId,
    String phoneNumber
) {}
