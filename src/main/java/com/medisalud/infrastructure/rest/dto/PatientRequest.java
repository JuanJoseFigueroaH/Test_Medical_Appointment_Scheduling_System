package com.medisalud.infrastructure.rest.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.PastOrPresent;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

import java.time.LocalDate;

@Schema(description = "Datos para crear o actualizar un paciente")
public record PatientRequest(
    @NotBlank(message = "El nombre completo es obligatorio")
    @Size(min = 3, max = 100, message = "El nombre debe tener entre 3 y 100 caracteres")
    @Schema(description = "Nombre completo del paciente", example = "Juan Pérez García")
    String fullName,
    
    @NotBlank(message = "El documento de identidad es obligatorio")
    @Size(min = 7, max = 20, message = "El documento de identidad debe tener entre 7 y 20 caracteres")
    @Schema(description = "Documento de identidad único", example = "12345678")
    String documentId,
    
    @NotBlank(message = "El teléfono es obligatorio")
    @Pattern(regexp = "^\\d{7,15}$", message = "El teléfono debe tener entre 7 y 15 dígitos")
    @Schema(description = "Número de teléfono", example = "5552001")
    String phone,
    
    @NotBlank(message = "El email es obligatorio")
    @Email(message = "El formato del email no es válido")
    @Size(max = 100, message = "El email no puede exceder 100 caracteres")
    @Schema(description = "Correo electrónico", example = "juan.perez@email.com")
    String email,
    
    @PastOrPresent(message = "La fecha de nacimiento no puede ser futura (RN-03)")
    @Schema(description = "Fecha de nacimiento (opcional, no puede ser futura)", example = "1990-05-15")
    LocalDate birthDate
) {}
