package com.arcabank.auth.dto;

public record LoginRequest(
    String username,
    String password
) {}
