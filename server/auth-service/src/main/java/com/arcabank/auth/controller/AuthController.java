package com.arcabank.auth.controller;

import com.arcabank.auth.dto.LoginRequest;
import com.arcabank.auth.dto.RegistrationRequest;
import com.arcabank.auth.dto.TokenResponse;
import com.arcabank.auth.exception.AppException;
import com.arcabank.auth.service.UserLoginService;
import com.arcabank.auth.service.UserRegistrationService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@Tag(name = "Authentication", description = "Public API for user registration, login, and token management")
@RestController
@RequestMapping("/api/v1/auth/public")
@RequiredArgsConstructor
public class AuthController {

    private final UserRegistrationService registrationService;
    private final UserLoginService loginService;

    @Operation(summary = "Register a new user", description = "Creates a new user profile, registers them in Keycloak, synchronizes data to the local database, and provisions an initial bank account via gRPC.")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "201", description = "User successfully registered"),
        @ApiResponse(responseCode = "400", description = "Validation error in input data"),
        @ApiResponse(responseCode = "409", description = "Conflict: User with this email or passport ID already exists"),
        @ApiResponse(responseCode = "500", description = "Internal server error during registration")
    })
    @PostMapping("/register")
    @ResponseStatus(HttpStatus.CREATED)
    public ResponseEntity<String> registerUser(@Valid @RequestBody RegistrationRequest request) {
        registrationService.registerUser(request);
        return ResponseEntity.status(HttpStatus.CREATED).body("User created successfully");
    }

    @Operation(summary = "Authenticate user", description = "Authenticates a user via email and password using Keycloak, returning JWT access and refresh tokens.")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Successfully authenticated"),
        @ApiResponse(responseCode = "400", description = "Invalid request format"),
        @ApiResponse(responseCode = "401", description = "Unauthorized: Invalid email or password"),
        @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    @PostMapping("/login")
    public TokenResponse loginUser(@RequestBody LoginRequest request) {
        return loginService.authenticate(request);
    }

    @Operation(summary = "Refresh access token", description = "Generates a new access token using a valid Keycloak refresh token.")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Token successfully refreshed"),
        @ApiResponse(responseCode = "400", description = "Bad request: Refresh token is missing or invalid"),
        @ApiResponse(responseCode = "401", description = "Unauthorized: Refresh token is expired or revoked"),
        @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    @PostMapping("/refresh")
    public ResponseEntity<TokenResponse> refreshToken(@RequestBody Map<String, String> body) {
        String refreshToken = body.get("refresh_token");

        if (refreshToken == null || refreshToken.isBlank()) {
            throw new AppException("Refresh token is required", "400", HttpStatus.BAD_REQUEST );
        }

        TokenResponse response = loginService.refreshToken(refreshToken);
        return ResponseEntity.ok(response);
    }
}
