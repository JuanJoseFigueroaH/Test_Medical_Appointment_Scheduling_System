package com.medisalud.domain.model.vo;

import java.util.Objects;
import java.util.regex.Pattern;

public final class Email {
    
    private static final Pattern EMAIL_PATTERN = Pattern.compile("^[\\w.-]+@[\\w.-]+\\.[a-zA-Z]{2,}$");
    
    private final String value;
    
    private Email(String value) {
        this.value = value;
    }
    
    public static Email of(String value) {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException("El email no puede estar vacío");
        }
        String trimmed = value.trim().toLowerCase();
        if (!EMAIL_PATTERN.matcher(trimmed).matches()) {
            throw new IllegalArgumentException("El formato del email no es válido: " + value);
        }
        return new Email(trimmed);
    }
    
    public static Email ofNullable(String value) {
        if (value == null || value.isBlank()) {
            return null;
        }
        return of(value);
    }
    
    public String getValue() {
        return value;
    }
    
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Email email = (Email) o;
        return Objects.equals(value, email.value);
    }
    
    @Override
    public int hashCode() {
        return Objects.hash(value);
    }
    
    @Override
    public String toString() {
        return value;
    }
}
