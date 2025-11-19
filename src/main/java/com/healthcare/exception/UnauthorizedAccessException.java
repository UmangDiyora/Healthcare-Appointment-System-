package com.healthcare.exception;

/**
 * Exception thrown when user attempts unauthorized access
 */
public class UnauthorizedAccessException extends RuntimeException {

    public UnauthorizedAccessException(String message) {
        super(message);
    }
}
