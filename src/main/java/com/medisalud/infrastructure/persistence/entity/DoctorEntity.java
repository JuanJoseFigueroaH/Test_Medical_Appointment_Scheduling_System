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

import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Table("doctors")
public class DoctorEntity implements Persistable<UUID> {
    @Id
    private UUID id;
    
    @Transient
    @Builder.Default
    private boolean isNew = true;
    
    @Column("full_name")
    private String fullName;
    
    private String specialty;
    
    private String phone;
    
    private String email;
    
    @Override
    public boolean isNew() {
        return isNew;
    }
}
