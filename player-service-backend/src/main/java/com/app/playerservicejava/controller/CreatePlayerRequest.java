package com.app.playerservicejava.controller;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;

public record CreatePlayerRequest(
        @NotBlank(message = "First name is required")
        @Pattern(regexp = "^[a-zA-Z\\s]+$", message = "First name must contain only letters and spaces")
        String firstName,
        
        @NotBlank(message = "Last name is required") 
        @Pattern(regexp = "^[a-zA-Z\\s]+$", message = "Last name must contain only letters and spaces")
        String lastName,
        
        @NotBlank(message = "Email is required")
        @Email(message = "Email must be a valid email address")
        String email
) {
}
