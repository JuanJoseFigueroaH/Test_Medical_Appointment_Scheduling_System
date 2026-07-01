package com.medisalud.domain.model.vo;

import java.util.Objects;

public final class DocumentId {
    
    private static final int MIN_LENGTH = 7;
    private static final int MAX_LENGTH = 20;
    
    private final String value;
    
    private DocumentId(String value) {
        this.value = value;
    }
    
    public static DocumentId of(String value) {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException("El documento de identidad no puede estar vacío");
        }
        String trimmed = value.trim().toUpperCase();
        if (trimmed.length() < MIN_LENGTH) {
            throw new IllegalArgumentException(
                    String.format("El documento de identidad debe tener al menos %d caracteres", MIN_LENGTH));
        }
        if (trimmed.length() > MAX_LENGTH) {
            throw new IllegalArgumentException(
                    String.format("El documento de identidad no puede exceder %d caracteres", MAX_LENGTH));
        }
        return new DocumentId(trimmed);
    }
    
    public String getValue() {
        return value;
    }
    
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        DocumentId that = (DocumentId) o;
        return Objects.equals(value, that.value);
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
