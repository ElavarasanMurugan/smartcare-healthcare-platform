package com.smartcare.user.dto;

import com.smartcare.user.config.StrictStringDeserializer;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import tools.jackson.databind.annotation.JsonDeserialize;

/**
 * Request payload for user authentication via {@code POST /api/v1/auth/login}.
 */
public record LoginRequest(
        @JsonDeserialize(using = StrictStringDeserializer.class)
        @NotBlank(message = "Email is required")
        @Email(message = "Email must be a valid email address")
        @Size(max = 254, message = "Email must not exceed 254 characters")
        String email,

        @JsonDeserialize(using = StrictStringDeserializer.class)
        @NotBlank(message = "Password is required")
        String password
) {
}
