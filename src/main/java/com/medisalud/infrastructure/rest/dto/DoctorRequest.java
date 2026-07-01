package com.medisalud.infrastructure.rest.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

@Schema(description = "Datos para crear o actualizar un médico")
public record DoctorRequest(
    @NotBlank(message = "El nombre completo es obligatorio")
    @Size(min = 3, max = 100, message = "El nombre debe tener entre 3 y 100 caracteres")
    @Schema(description = "Nombre completo del médico", example = "Dra. María González")
    String fullName,
    
    @NotBlank(message = "La especialidad es obligatoria")
    @Size(max = 50, message = "La especialidad no puede exceder 50 caracteres")
    @Schema(description = "Especialidad médica", example = "Cardiología")
    String specialty,
    
    @Pattern(regexp = "^$|^\\d{7,15}$", message = "El teléfono debe tener entre 7 y 15 dígitos")
    @Schema(description = "Número de teléfono (opcional)", example = "5551001")
    String phone,
    
    @Email(message = "El formato del email no es válido")
    @Size(max = 100, message = "El email no puede exceder 100 caracteres")
    @Schema(description = "Correo electrónico (opcional)", example = "maria.gonzalez@medisalud.com")
    String email
) {}
