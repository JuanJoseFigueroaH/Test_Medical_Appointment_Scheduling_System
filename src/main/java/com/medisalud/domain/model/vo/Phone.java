package com.medisalud.domain.model.vo;

import java.util.Objects;
import java.util.regex.Pattern;

public final class Phone {
    
    private static final Pattern PHONE_PATTERN = Pattern.compile("\\d{7,15}");
    
    private final String value;
    
    private Phone(String value) {
        this.value = value;
    }
    
    public static Phone of(String value) {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException("El teléfono no puede estar vacío");
        }
        String digits = value.replaceAll("[^0-9]", "");
        if (!PHONE_PATTERN.matcher(digits).matches()) {
            throw new IllegalArgumentException("El teléfono debe tener entre 7 y 15 dígitos: " + value);
        }
        return new Phone(digits);
    }
    
    public static Phone ofNullable(String value) {
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
        Phone phone = (Phone) o;
        return Objects.equals(value, phone.value);
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
