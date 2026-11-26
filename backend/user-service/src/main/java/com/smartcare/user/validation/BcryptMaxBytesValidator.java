package com.smartcare.user.validation;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

import java.nio.charset.StandardCharsets;

/**
 * Validator for {@link BcryptMaxBytes} that checks UTF-8 byte length <= 72.
 */
public class BcryptMaxBytesValidator implements ConstraintValidator<BcryptMaxBytes, String> {

    private static final int BCRYPT_MAX_BYTES = 72;

    @Override
    public boolean isValid(String value, ConstraintValidatorContext context) {
        if (value == null) {
            return true; // @NotBlank handles null values
        }
        return value.getBytes(StandardCharsets.UTF_8).length <= BCRYPT_MAX_BYTES;
    }
}
