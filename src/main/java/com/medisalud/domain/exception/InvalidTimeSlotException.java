package com.medisalud.domain.exception;

public class InvalidTimeSlotException extends BusinessRuleException {
    public InvalidTimeSlotException(String message) {
        super("INVALID_TIME_SLOT", message);
    }
}
