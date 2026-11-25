package com.smartcare.user.dto;

import com.smartcare.user.entity.User;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/**
 * Request payload for creating or updating a user.
 */
public record UserRequest(
        @NotBlank(message = "Full name is required")
        @Size(max = 100, message = "Full name must not exceed 100 characters")
        String fullName,

        @NotBlank(message = "Email is required")
        @Email(message = "Email must be a valid email address")
        @Size(max = 254, message = "Email must not exceed 254 characters")
        String email,

        @Size(max = 20, message = "Phone number must not exceed 20 characters")
        String phoneNumber
) {

    /**
     * Converts this DTO into a new {@link User} entity with the provided normalized email.
     */
    public User toEntity(String normalizedEmail) {
        String trimmedName = fullName != null ? fullName.trim() : null;
        String trimmedPhone = phoneNumber != null ? phoneNumber.trim() : null;
        return new User(trimmedName, normalizedEmail, trimmedPhone);
    }
}
