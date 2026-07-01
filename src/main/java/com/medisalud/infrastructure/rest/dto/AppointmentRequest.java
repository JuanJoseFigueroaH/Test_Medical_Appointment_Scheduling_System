package com.medisalud.infrastructure.rest.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;

import java.time.LocalDateTime;
import java.util.UUID;

@Schema(description = "Datos para crear una cita")
public record AppointmentRequest(
    @NotNull(message = "El ID del paciente es obligatorio")
    @Schema(description = "ID del paciente", example = "550e8400-e29b-41d4-a716-446655440000")
    UUID patientId,
    
    @NotNull(message = "El ID del médico es obligatorio")
    @Schema(description = "ID del médico", example = "550e8400-e29b-41d4-a716-446655440001")
    UUID doctorId,
    
    @NotNull(message = "La fecha y hora es obligatoria")
    @Schema(description = "Fecha y hora de la cita en formato ISO 8601 (YYYY-MM-DDTHH:mm:ss). " +
            "Debe ser un día hábil (lunes a sábado, excepto festivos) dentro del horario de atención.", 
            example = "2024-12-20T10:00:00",
            pattern = "yyyy-MM-dd'T'HH:mm:ss")
    LocalDateTime dateTime
) {}
