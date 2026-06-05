package com.personal.pulseguardserverapi.dto.request;

import jakarta.validation.constraints.*;
import lombok.Data;

@Data
public class RegisterRequest {

    @NotBlank(message = "Email is required")
    @Email(message = "Email must be a valid email address")
    private String email;

    @NotBlank(message = "Password is required")
    @Size(min = 8, message = "Password must be at least 8 characters")
    private String password;

    @NotBlank(message = "Name is required")
    private String name;

    private String sex;

    @Min(value = 0, message = "Age must be positive")
    @Max(value = 150, message = "Age must be realistic")
    private Integer age;

    private boolean consentGiven;
}
