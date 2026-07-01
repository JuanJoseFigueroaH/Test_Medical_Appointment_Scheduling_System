package com.medisalud.infrastructure.rest.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Respuesta con datos de la cita")
public class AppointmentResponse {
    
    @Schema(description = "ID único de la cita")
    private UUID id;
    
    @Schema(description = "ID del paciente")
    private UUID patientId;
    
    @Schema(description = "ID del médico")
    private UUID doctorId;
    
    @Schema(description = "Fecha y hora de la cita")
    private LocalDateTime dateTime;
    
    @Schema(description = "Estado de la cita", example = "PROGRAMADA")
    private String status;
    
    @Schema(description = "Fecha y hora de cancelación (si aplica)")
    private LocalDateTime cancellationDateTime;
    
    @Schema(description = "Fecha de creación")
    private LocalDateTime createdAt;
    
    @Schema(description = "Fecha de última actualización")
    private LocalDateTime updatedAt;
}
