package com.medisalud.domain.exception;

public class BusinessRuleException extends DomainException {
    public BusinessRuleException(String code, String message) {
        super(code, message);
    }
}
