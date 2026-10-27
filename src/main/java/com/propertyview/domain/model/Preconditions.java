package com.propertyview.domain.model;

import com.propertyview.domain.exception.DomainValidationException;

/**
 * a utility helper class for verifying domain object invariants—checking for nulls,
 * empty strings, and invalid values
 */
final class Preconditions {
    private Preconditions() {}

    static String requireText(String value, String field) {
        if (value == null || value.isBlank()) {
            throw new DomainValidationException(field + " must not be blank");
        }
        return value.strip();
    }

    static String optionalText(String value) {
        return value == null || value.isBlank() ? null : value.strip();
    }

    static <T> T requireNonNull(T value, String field) {
        if (value == null) {
            throw new DomainValidationException(field + " must not be null");
        }
        return value;
    }

    static int requirePositive(int value, String field) {
        if (value <= 0) {
            throw new DomainValidationException(field + " must be positive");
        }
        return value;
    }
}
