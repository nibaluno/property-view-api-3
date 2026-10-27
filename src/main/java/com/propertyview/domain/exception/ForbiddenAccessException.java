package com.propertyview.domain.exception;

/**
 * thrown when an authenticated user attempts to access or modify another owner's hotel (IDOR protection)
 */
public final class ForbiddenAccessException extends DomainException {
    public ForbiddenAccessException(long hotelId, String principal) {
        super("Access denied: user '" + principal + "' is not authorized to manage hotel with id " + hotelId);
    }
}
