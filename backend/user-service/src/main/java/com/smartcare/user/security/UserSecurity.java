package com.smartcare.user.security;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

/**
 * Helper bean for method‑level security expressions.
 * Allows checking if the currently authenticated user is the owner of a resource.
 */
@Component("userSecurity")
public class UserSecurity {

    /**
     * Returns {@code true} when the authenticated principal's identifier matches the supplied {@code userId}.
     * The JWT authentication filter sets the principal to the user's {@code Long} id.
     */
    public boolean isOwner(Long userId) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || auth.getPrincipal() == null) {
            return false;
        }
        Object principal = auth.getPrincipal();
        if (principal instanceof Long) {
            return principal.equals(userId);
        }
        // In case the principal is a String representation of the id.
        try {
            return Long.parseLong(principal.toString()) == userId;
        } catch (NumberFormatException ex) {
            return false;
        }
    }
}
