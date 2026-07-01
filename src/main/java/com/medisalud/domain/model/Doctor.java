package com.medisalud.domain.model;

import com.medisalud.domain.model.vo.Email;
import com.medisalud.domain.model.vo.Phone;
import lombok.Builder;
import lombok.Getter;

import java.util.UUID;

@Getter
@Builder(toBuilder = true)
public class Doctor {
    private final UUID id;
    private final String fullName;
    private final String specialty;
    private final Phone phone;
    private final Email email;
    
    public Doctor(UUID id, String fullName, String specialty, Phone phone, Email email) {
        validateFullName(fullName);
        validateSpecialty(specialty);
        
        this.id = id;
        this.fullName = fullName;
        this.specialty = specialty;
        this.phone = phone;
        this.email = email;
    }
    
    private void validateFullName(String fullName) {
        if (fullName == null || fullName.trim().length() < 3) {
            throw new IllegalArgumentException("El nombre completo debe tener al menos 3 caracteres");
        }
        if (fullName.length() > 100) {
            throw new IllegalArgumentException("El nombre completo no puede exceder 100 caracteres");
        }
    }
    
    private void validateSpecialty(String specialty) {
        if (specialty == null || specialty.trim().isEmpty()) {
            throw new IllegalArgumentException("La especialidad es obligatoria");
        }
    }
    
    public String getPhoneValue() {
        return phone != null ? phone.getValue() : null;
    }
    
    public String getEmailValue() {
        return email != null ? email.getValue() : null;
    }
}
