package com.medisalud.infrastructure.rest.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;

import java.time.LocalDateTime;
import java.util.UUID;

@Builder
@Schema(description = "Información de una penalización")
public record PenaltyResponse(
    @Schema(description = "ID de la penalización")
    UUID id,
    
    @Schema(description = "ID del paciente penalizado")
    UUID patientId,
    
    @Schema(description = "ID de la cita que generó la penalización")
    UUID appointmentId,
    
    @Schema(description = "Fecha y hora de la penalización (ISO 8601)", example = "2024-12-20T10:00:00")
    LocalDateTime penaltyDateTime,
    
    @Schema(description = "Razón de la penalización")
    String reason
) {}
