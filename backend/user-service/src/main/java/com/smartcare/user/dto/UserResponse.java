package com.smartcare.user.dto;

import com.smartcare.user.entity.Role;
import com.smartcare.user.entity.User;

import java.time.Instant;

/**
 * Response payload representing a user returned by the API.
 */
public record UserResponse(
        Long id,
        String fullName,
        String email,
        String phoneNumber,
        Role role,
        Instant createdAt,
        Instant updatedAt
) {

    /**
     * Maps a {@link User} JPA entity to a {@link UserResponse} DTO.
     */
    public static UserResponse fromEntity(User user) {
        return new UserResponse(
                user.getId(),
                user.getFullName(),
                user.getEmail(),
                user.getPhoneNumber(),
                user.getEffectiveRole(),
                user.getCreatedAt(),
                user.getUpdatedAt()
        );
    }
}
