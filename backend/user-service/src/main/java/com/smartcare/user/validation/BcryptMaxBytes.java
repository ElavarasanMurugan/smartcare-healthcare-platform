package com.smartcare.user.validation;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Validates that a string does not exceed 72 UTF-8 bytes (BCrypt's maximum input length).
 */
@Documented
@Constraint(validatedBy = BcryptMaxBytesValidator.class)
@Target({ElementType.FIELD, ElementType.RECORD_COMPONENT, ElementType.PARAMETER})
@Retention(RetentionPolicy.RUNTIME)
public @interface BcryptMaxBytes {

    String message() default "Password must not exceed 72 UTF-8 bytes";

    Class<?>[] groups() default {};

    Class<? extends Payload>[] payload() default {};
}
