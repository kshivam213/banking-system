package com.banking.system.integration.models;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;

public record LoginRequest(
        @NotBlank(message = "email is required")
        @Email(message = "email is invalid")
        String email,

        @NotBlank(message = "pin is required")
        @Pattern(regexp = "^[0-9]{4,6}$", message = "pin must be 4-6 digits")
        String pin
) {
}
