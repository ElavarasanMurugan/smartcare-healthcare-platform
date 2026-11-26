package com.smartcare.user.dto;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class UserRegistrationRequestValidationTest {

    private static ValidatorFactory validatorFactory;
    private static Validator validator;

    @BeforeAll
    static void setUpValidator() {
        validatorFactory = Validation.buildDefaultValidatorFactory();
        validator = validatorFactory.getValidator();
    }

    @AfterAll
    static void closeValidator() {
        if (validatorFactory != null) {
            validatorFactory.close();
        }
    }

    @Test
    void validRegistrationRequest_hasNoViolations() {
        UserRegistrationRequest request = new UserRegistrationRequest(
                "Aarav Sharma",
                "aarav@example.com",
                "+91-9876543210",
                "StrongPass123!"
        );
        Set<ConstraintViolation<UserRegistrationRequest>> violations = validator.validate(request);
        assertTrue(violations.isEmpty());
    }

    @Test
    void missingPassword_failsValidation() {
        UserRegistrationRequest request = new UserRegistrationRequest(
                "Aarav Sharma",
                "aarav@example.com",
                null,
                null
        );
        Set<ConstraintViolation<UserRegistrationRequest>> violations = validator.validate(request);
        assertFalse(violations.isEmpty());
    }

    @Test
    void blankPassword_failsValidation() {
        UserRegistrationRequest request = new UserRegistrationRequest(
                "Aarav Sharma",
                "aarav@example.com",
                null,
                "        "
        );
        Set<ConstraintViolation<UserRegistrationRequest>> violations = validator.validate(request);
        assertFalse(violations.isEmpty());
    }

    @Test
    void passwordShorterThan8Chars_failsValidation() {
        UserRegistrationRequest request = new UserRegistrationRequest(
                "Aarav Sharma",
                "aarav@example.com",
                null,
                "Short1!"
        );
        Set<ConstraintViolation<UserRegistrationRequest>> violations = validator.validate(request);
        assertFalse(violations.isEmpty());
    }

    @Test
    void passwordExceeding72AsciiBytes_failsValidation() {
        String longPassword = "A".repeat(73);
        UserRegistrationRequest request = new UserRegistrationRequest(
                "Aarav Sharma",
                "aarav@example.com",
                null,
                longPassword
        );
        Set<ConstraintViolation<UserRegistrationRequest>> violations = validator.validate(request);
        assertFalse(violations.isEmpty());
    }

    @Test
    void passwordExceeding72Utf8BytesWithMultibyteChars_failsValidation() {
        // 20 4-byte emojis = 20 characters, but 80 UTF-8 bytes (> 72 bytes)
        String multibytePassword = "\uD83D\uDE00".repeat(20);
        UserRegistrationRequest request = new UserRegistrationRequest(
                "Aarav Sharma",
                "aarav@example.com",
                null,
                multibytePassword
        );
        Set<ConstraintViolation<UserRegistrationRequest>> violations = validator.validate(request);
        assertFalse(violations.isEmpty());
    }
}
