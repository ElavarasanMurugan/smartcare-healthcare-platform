package com.smartcare.user.dto;

import com.smartcare.user.config.StrictStringDeserializer;
import com.smartcare.user.entity.User;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import tools.jackson.databind.annotation.JsonDeserialize;

/**
 * Request payload for creating or updating a user.
 */
public record UserRequest(
        @JsonDeserialize(using = StrictStringDeserializer.class)
        @NotBlank(message = "Full name is required")
        @Size(max = 100, message = "Full name must not exceed 100 characters")
        String fullName,

        @JsonDeserialize(using = StrictStringDeserializer.class)
        @NotBlank(message = "Email is required")
        @Email(message = "Email must be a valid email address")
        @Size(max = 254, message = "Email must not exceed 254 characters")
        String email,

        @JsonDeserialize(using = StrictStringDeserializer.class)
        @Size(max = 20, message = "Phone number must not exceed 20 characters")
        @Pattern(
                regexp = "^$|^[+]?[0-9][0-9\\s\\-()]{6,19}$",
                message = "Phone number must be 7 to 20 characters and contain only digits, spaces, hyphens, parentheses, or a leading +"
        )
        String phoneNumber
) {

    /**
     * Converts this DTO into a new {@link User} entity with the provided normalized email.
     */
    public User toEntity(String normalizedEmail) {
        String trimmedName = fullName != null ? fullName.trim() : null;
        String normalizedPhone = (phoneNumber != null && !phoneNumber.isBlank()) ? phoneNumber.trim() : null;
        return new User(trimmedName, normalizedEmail, normalizedPhone);
    }
}
