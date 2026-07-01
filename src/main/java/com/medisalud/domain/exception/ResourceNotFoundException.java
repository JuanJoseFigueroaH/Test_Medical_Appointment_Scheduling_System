package com.medisalud.domain.exception;

public class ResourceNotFoundException extends DomainException {
    public ResourceNotFoundException(String resourceType, String identifier) {
        super("NOT_FOUND", String.format("%s con identificador '%s' no encontrado", resourceType, identifier));
    }
}
