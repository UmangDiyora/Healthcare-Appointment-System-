package com.healthcare.exception;

/**
 * Exception thrown for invalid request data
 */
public class InvalidRequestException extends RuntimeException {

    public InvalidRequestException(String message) {
        super(message);
    }
}
