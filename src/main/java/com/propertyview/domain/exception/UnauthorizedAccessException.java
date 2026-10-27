package com.propertyview.domain.exception;

/**
 * thrown when a request accesses a protected resource without valid authentication credentials
 */
public final class UnauthorizedAccessException extends DomainException {
    public UnauthorizedAccessException(String message) {
        super(message);
    }
}
