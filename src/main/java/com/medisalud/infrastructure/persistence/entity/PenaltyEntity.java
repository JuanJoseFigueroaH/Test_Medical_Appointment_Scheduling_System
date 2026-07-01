package com.medisalud.infrastructure.persistence.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Column;
import org.springframework.data.relational.core.mapping.Table;

import java.time.LocalDateTime;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Table("penalties")
public class PenaltyEntity {
    @Id
    private UUID id;
    
    @Column("patient_id")
    private UUID patientId;
    
    @Column("appointment_id")
    private UUID appointmentId;
    
    @Column("penalty_date_time")
    private LocalDateTime penaltyDateTime;
    
    private String reason;
}
