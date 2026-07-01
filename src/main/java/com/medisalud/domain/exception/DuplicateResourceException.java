package com.medisalud.domain.exception;

public class DuplicateResourceException extends DomainException {
    public DuplicateResourceException(String message) {
        super("DUPLICATE", message);
    }
}
