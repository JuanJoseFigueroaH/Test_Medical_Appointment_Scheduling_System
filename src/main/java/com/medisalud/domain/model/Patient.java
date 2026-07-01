package com.medisalud.domain.model;

import com.medisalud.domain.model.vo.DocumentId;
import com.medisalud.domain.model.vo.Email;
import com.medisalud.domain.model.vo.Phone;
import lombok.Builder;
import lombok.Getter;

import java.time.Clock;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.Period;
import java.util.UUID;

@Getter
public class Patient {
    private final UUID id;
    private final String fullName;
    private final DocumentId documentId;
    private final Phone phone;
    private final Email email;
    private final LocalDate birthDate;
    private final LocalDateTime registrationDate;
    
    @Builder(toBuilder = true)
    private Patient(UUID id, String fullName, DocumentId documentId, Phone phone, 
                    Email email, LocalDate birthDate, LocalDateTime registrationDate) {
        validateFullName(fullName);
        
        this.id = id;
        this.fullName = fullName;
        this.documentId = documentId;
        this.phone = phone;
        this.email = email;
        this.birthDate = birthDate;
        this.registrationDate = registrationDate != null ? registrationDate : LocalDateTime.now();
    }
    
    public static Patient create(UUID id, String fullName, DocumentId documentId, Phone phone,
                                  Email email, LocalDate birthDate, LocalDateTime registrationDate,
                                  Clock clock) {
        validateBirthDate(birthDate, clock);
        return Patient.builder()
                .id(id)
                .fullName(fullName)
                .documentId(documentId)
                .phone(phone)
                .email(email)
                .birthDate(birthDate)
                .registrationDate(registrationDate)
                .build();
    }
    
    public static Patient create(UUID id, String fullName, DocumentId documentId, Phone phone,
                                  Email email, LocalDate birthDate, LocalDateTime registrationDate) {
        return create(id, fullName, documentId, phone, email, birthDate, registrationDate, Clock.systemDefaultZone());
    }
    
    private void validateFullName(String fullName) {
        if (fullName == null || fullName.trim().length() < 3) {
            throw new IllegalArgumentException("El nombre completo debe tener al menos 3 caracteres");
        }
    }
    
    private static void validateBirthDate(LocalDate birthDate, Clock clock) {
        if (birthDate != null && birthDate.isAfter(LocalDate.now(clock))) {
            throw new IllegalArgumentException("La fecha de nacimiento no puede ser futura (RN-03)");
        }
    }
    
    public int getAge(Clock clock) {
        if (birthDate == null) return 0;
        return Period.between(birthDate, LocalDate.now(clock)).getYears();
    }
    
    public int getAge() {
        return getAge(Clock.systemDefaultZone());
    }
    
    public boolean canSchedule() {
        return fullName != null && documentId != null && phone != null && email != null;
    }
    
    public String getDocumentIdValue() {
        return documentId != null ? documentId.getValue() : null;
    }
    
    public String getPhoneValue() {
        return phone != null ? phone.getValue() : null;
    }
    
    public String getEmailValue() {
        return email != null ? email.getValue() : null;
    }
}
