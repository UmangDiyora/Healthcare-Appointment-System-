package com.healthcare.exception;

/**
 * Exception thrown when appointment booking conflicts with existing appointment
 */
public class AppointmentConflictException extends RuntimeException {

    public AppointmentConflictException(String message) {
        super(message);
    }
}
