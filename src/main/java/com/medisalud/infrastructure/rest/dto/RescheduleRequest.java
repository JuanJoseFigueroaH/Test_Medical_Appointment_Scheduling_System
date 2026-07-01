package com.medisalud.infrastructure.rest.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;

import java.time.LocalDateTime;

@Schema(description = "Datos para reprogramar una cita")
public record RescheduleRequest(
    @NotNull(message = "La nueva fecha y hora es obligatoria")
    @Schema(description = "Nueva fecha y hora de la cita en formato ISO 8601 (YYYY-MM-DDTHH:mm:ss). " +
            "Debe ser un día hábil (lunes a sábado, excepto festivos) dentro del horario de atención.", 
            example = "2024-12-21T14:30:00",
            pattern = "yyyy-MM-dd'T'HH:mm:ss")
    LocalDateTime newDateTime
) {}
