package com.medisalud.infrastructure.rest.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;

import java.util.List;
import java.util.UUID;

@Builder
@Schema(description = "Resumen de penalizaciones de un paciente")
public record PatientPenaltySummary(
    @Schema(description = "ID del paciente")
    UUID patientId,
    
    @Schema(description = "Número de penalizaciones activas en los últimos 30 días")
    long activePenaltyCount,
    
    @Schema(description = "Indica si el paciente está bloqueado para agendar citas")
    boolean blocked,
    
    @Schema(description = "Lista de penalizaciones activas")
    List<PenaltyResponse> penalties
) {}
