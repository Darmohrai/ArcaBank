package com.arcabank.auth.controller;

import com.arcabank.auth.dto.UserPhoneResponse;
import com.arcabank.auth.dto.UserResponse;
import com.arcabank.auth.exception.AppException;
import com.arcabank.auth.model.User;
import com.arcabank.auth.repository.UserRepository;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@Tag(name = "Users", description = "API for retrieving user profile information")
@RestController
@RequestMapping("/api/v1/users")
@RequiredArgsConstructor
public class UserController {

    private final UserRepository userRepository;

    @Operation(summary = "Get user by ID", description = "Retrieves detailed profile information for a specific user based on their unique identifier.")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "User successfully found"),
        @ApiResponse(responseCode = "401", description = "Unauthorized access"),
        @ApiResponse(responseCode = "404", description = "User not found"),
        @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    @GetMapping("/{userId}")
    public ResponseEntity<UserResponse> getUserById(@PathVariable UUID userId) {

        User user = userRepository.findById(userId)
            .orElseThrow(() -> new AppException("User not found", "USER_NOT_FOUND", HttpStatus.NOT_FOUND));

        UserResponse response = new UserResponse(
            user.getId(),
            user.getFirstName(),
            user.getLastName(),
            user.getEmail(),
            user.getPassportId(),
            user.getPhoneNumber()
        );

        return ResponseEntity.ok(response);
    }

    @Operation(summary = "Get user by phone number", description = "Retrieves basic user information (ID, name, and phone) based on their registered phone number. Often used for peer-to-peer interactions like adding members to a chest.")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "User successfully found"),
        @ApiResponse(responseCode = "401", description = "Unauthorized access"),
        @ApiResponse(responseCode = "404", description = "User not found"),
        @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    @GetMapping("/phone/{phone_number}")
    public ResponseEntity<UserPhoneResponse> getUserByPhone(@PathVariable("phone_number") String phone) {

        User user = userRepository.findByPhone(phone)
            .orElseThrow(() -> new AppException("User not found", "USER_NOT_FOUND", HttpStatus.NOT_FOUND));

        UserPhoneResponse response = new UserPhoneResponse(
            user.getId(),
            user.getFirstName(),
            user.getLastName(),
            user.getPhoneNumber()
        );

        return ResponseEntity.ok(response);
    }
}
