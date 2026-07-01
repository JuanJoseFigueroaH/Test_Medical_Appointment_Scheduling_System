package com.medisalud.domain.exception;

public class AppointmentConflictException extends BusinessRuleException {
    public AppointmentConflictException(String message) {
        super("APPOINTMENT_CONFLICT", message);
    }
}
