package com.smartcare.user.exception;

/**
 * Thrown when login fails due to an unknown email or incorrect password.
 * Uses a generic message so attackers cannot enumerate registered email addresses.
 */
public class InvalidCredentialsException extends RuntimeException {

    public InvalidCredentialsException() {
        super("Invalid email or password.");
    }
}
