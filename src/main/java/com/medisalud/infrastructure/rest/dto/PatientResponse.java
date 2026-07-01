package com.medisalud.infrastructure.rest.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Respuesta con datos del paciente")
public class PatientResponse {
    
    @Schema(description = "ID único del paciente")
    private UUID id;
    
    @Schema(description = "Nombre completo del paciente")
    private String fullName;
    
    @Schema(description = "Documento de identidad")
    private String documentId;
    
    @Schema(description = "Número de teléfono")
    private String phone;
    
    @Schema(description = "Correo electrónico")
    private String email;
    
    @Schema(description = "Fecha de nacimiento")
    private LocalDate birthDate;
}
