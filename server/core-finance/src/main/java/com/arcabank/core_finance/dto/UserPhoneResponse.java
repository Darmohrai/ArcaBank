package com.arcabank.core_finance.dto;

import java.util.UUID;

public record UserPhoneResponse(
    UUID id,
    String firstName,
    String lastName,
    String phoneNumber
) {
}
