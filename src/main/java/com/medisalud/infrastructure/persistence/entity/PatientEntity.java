package com.medisalud.infrastructure.persistence.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.Transient;
import org.springframework.data.domain.Persistable;
import org.springframework.data.relational.core.mapping.Column;
import org.springframework.data.relational.core.mapping.Table;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Table("patients")
public class PatientEntity implements Persistable<UUID> {
    @Id
    private UUID id;
    
    @Transient
    @Builder.Default
    private boolean isNew = true;
    
    @Column("full_name")
    private String fullName;
    
    @Column("document_id")
    private String documentId;
    
    private String phone;
    
    private String email;
    
    @Column("birth_date")
    private LocalDate birthDate;
    
    @Column("registration_date")
    private LocalDateTime registrationDate;
    
    @Override
    public boolean isNew() {
        return isNew;
    }
}
