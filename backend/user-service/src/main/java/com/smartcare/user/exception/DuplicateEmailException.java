package com.smartcare.user.exception;

/**
 * Thrown when attempting to create or update a user with an email that is already in use.
 */
public class DuplicateEmailException extends RuntimeException {

    public DuplicateEmailException(String email) {
        super("A user with email '" + email + "' already exists.");
    }
}
