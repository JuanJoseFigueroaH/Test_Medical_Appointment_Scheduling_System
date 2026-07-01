package com.medisalud.infrastructure.persistence.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.Transient;
import org.springframework.data.annotation.Version;
import org.springframework.data.domain.Persistable;
import org.springframework.data.relational.core.mapping.Column;
import org.springframework.data.relational.core.mapping.Table;

import java.time.LocalDateTime;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Table("appointments")
public class AppointmentEntity implements Persistable<UUID> {
    @Id
    private UUID id;
    
    @Transient
    @Builder.Default
    private boolean isNew = true;
    
    @Version
    private Long version;
    
    @Column("patient_id")
    private UUID patientId;
    
    @Column("doctor_id")
    private UUID doctorId;
    
    @Column("date_time")
    private LocalDateTime dateTime;
    
    private String status;
    
    @Column("cancellation_date_time")
    private LocalDateTime cancellationDateTime;
    
    @Column("created_at")
    private LocalDateTime createdAt;
    
    @Column("updated_at")
    private LocalDateTime updatedAt;
    
    @Override
    public boolean isNew() {
        return isNew;
    }
}
