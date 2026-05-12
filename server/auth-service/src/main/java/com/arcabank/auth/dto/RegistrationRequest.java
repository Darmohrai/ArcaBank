package com.arcabank.auth.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public record RegistrationRequest(

    @NotBlank(message = "Passport ID cannot be empty")
    @Size(min = 10, max = 10, message = "Passport ID must have 10 numbers")
    String passport_id,

    @NotBlank(message = "Email is required")
    @Email(message = "Invalid email format")
    String email,

    @NotBlank(message = "First name is required")
    @Size(min = 2, max = 50, message = "First name is too short or too long")
    String firstName,

    @NotBlank(message = "Last name is required")
    @Size(min = 2, max = 50, message = "Last name is too short or too long")
    String lastName,

    @NotBlank(message = "Phone number is required")
    @Pattern(
        regexp = "^\\+380\\d{9}$",
        message = "Phone number must start with +380 and contain exactly 9 digits after it"
    )
    String phoneNumber,

    @NotBlank(message = "Password is required")
    @Pattern(
        regexp = "^(?=.*[0-9])(?=.*[a-z])(?=.*[A-Z])(?=.*[@#$%^&+=!]).{8,}$",
        message = "Password must contain at least 8 characters, one uppercase, one lowercase, one number and one special character"
    )
    String password
//
//    @NotBlank(message = "PIN code is required")
//    @Pattern(regexp = "^\\d{4}$", message = "PIN must be exactly 4 digits")
//    String pin
) {
}
