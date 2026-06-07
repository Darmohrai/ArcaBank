package com.arcabank.auth.dto;

import java.util.UUID;

public record UserPhoneResponse(
    UUID id,
    String firstName,
    String lastName,
    String phoneNumber
) {
}
