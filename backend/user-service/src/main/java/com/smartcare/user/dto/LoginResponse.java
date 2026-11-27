package com.smartcare.user.dto;

import com.smartcare.user.entity.User;

/**
 * Response payload returned after successful user login.
 * Contains the JWT access token, token metadata, and non-sensitive user profile details.
 */
public record LoginResponse(
        String accessToken,
        String tokenType,
        long expiresIn,
        UserSummary user
) {

    /**
     * Non-sensitive user summary included in login responses.
     */
    public record UserSummary(
            Long id,
            String fullName,
            String email
    ) {
        public static UserSummary fromEntity(User user) {
            return new UserSummary(user.getId(), user.getFullName(), user.getEmail());
        }
    }
}
