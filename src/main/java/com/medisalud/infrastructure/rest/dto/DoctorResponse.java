package com.medisalud.infrastructure.rest.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Respuesta con datos del médico")
public class DoctorResponse {
    
    @Schema(description = "ID único del médico")
    private UUID id;
    
    @Schema(description = "Nombre completo del médico")
    private String fullName;
    
    @Schema(description = "Especialidad médica")
    private String specialty;
    
    @Schema(description = "Número de teléfono")
    private String phone;
    
    @Schema(description = "Correo electrónico")
    private String email;
}
