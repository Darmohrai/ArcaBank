package com.arcabank.auth.dto;

public record LoginRequest(
    String email,
    String password
) {}
