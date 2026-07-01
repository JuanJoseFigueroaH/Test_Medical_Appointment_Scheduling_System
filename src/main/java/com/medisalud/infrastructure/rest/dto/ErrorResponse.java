package com.medisalud.infrastructure.rest.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Respuesta de error")
public class ErrorResponse {
    
    @Schema(description = "Código de error")
    private String code;
    
    @Schema(description = "Mensaje de error")
    private String message;
    
    @Schema(description = "Detalles adicionales del error")
    private List<String> details;
    
    @Schema(description = "Timestamp del error")
    private LocalDateTime timestamp;
    
    @Schema(description = "Ruta de la solicitud")
    private String path;
}
