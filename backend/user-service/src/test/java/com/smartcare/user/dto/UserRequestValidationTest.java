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

class UserRequestValidationTest {

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
    void validRequest_hasNoViolations() {
        UserRequest request = new UserRequest("Riya Patel", "riya.patel@example.com", "+91-9876543210");
        Set<ConstraintViolation<UserRequest>> violations = validator.validate(request);
        assertTrue(violations.isEmpty());
    }

    @Test
    void validRequestWithoutPhoneNumber_hasNoViolations() {
        UserRequest request = new UserRequest("Riya Patel", "riya.patel@example.com", null);
        Set<ConstraintViolation<UserRequest>> violations = validator.validate(request);
        assertTrue(violations.isEmpty());
    }

    @Test
    void validInternationalPhoneWithSpacesAndParentheses_hasNoViolations() {
        UserRequest request = new UserRequest("Riya Patel", "riya.patel@example.com", "+1 (555) 123-4567");
        Set<ConstraintViolation<UserRequest>> violations = validator.validate(request);
        assertTrue(violations.isEmpty());
    }

    @Test
    void blankFullName_failsValidation() {
        UserRequest request = new UserRequest("   ", "riya@example.com", null);
        Set<ConstraintViolation<UserRequest>> violations = validator.validate(request);
        assertFalse(violations.isEmpty());
    }

    @Test
    void fullNameExceedingMaxLength_failsValidation() {
        String longName = "A".repeat(101);
        UserRequest request = new UserRequest(longName, "riya@example.com", null);
        Set<ConstraintViolation<UserRequest>> violations = validator.validate(request);
        assertFalse(violations.isEmpty());
    }

    @Test
    void invalidEmail_failsValidation() {
        UserRequest request = new UserRequest("Riya Patel", "not-an-email", null);
        Set<ConstraintViolation<UserRequest>> violations = validator.validate(request);
        assertFalse(violations.isEmpty());
    }

    @Test
    void emailExceedingMaxLength_failsValidation() {
        String localPart = "a".repeat(245);
        String longEmail = localPart + "@example.com"; // 257 chars > 254
        UserRequest request = new UserRequest("Riya Patel", longEmail, null);
        Set<ConstraintViolation<UserRequest>> violations = validator.validate(request);
        assertFalse(violations.isEmpty());
    }

    @Test
    void invalidPhoneNumberFormat_failsValidation() {
        UserRequest request = new UserRequest("Riya Patel", "riya@example.com", "invalid-phone");
        Set<ConstraintViolation<UserRequest>> violations = validator.validate(request);
        assertFalse(violations.isEmpty());
    }

    @Test
    void phoneNumberExceedingMaxLength_failsValidation() {
        UserRequest request = new UserRequest("Riya Patel", "riya@example.com", "123456789012345678901");
        Set<ConstraintViolation<UserRequest>> violations = validator.validate(request);
        assertFalse(violations.isEmpty());
    }
}
