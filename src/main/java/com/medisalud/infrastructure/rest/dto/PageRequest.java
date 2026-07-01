package com.medisalud.infrastructure.rest.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Parámetros de paginación")
public class PageRequest {
    
    @Min(value = 0, message = "La página debe ser mayor o igual a 0")
    @Schema(description = "Número de página (0-indexed)", example = "0", defaultValue = "0")
    @Builder.Default
    private int page = 0;
    
    @Min(value = 1, message = "El tamaño debe ser al menos 1")
    @Max(value = 100, message = "El tamaño no puede exceder 100")
    @Schema(description = "Tamaño de página", example = "20", defaultValue = "20")
    @Builder.Default
    private int size = 20;
    
    public int getOffset() {
        return page * size;
    }
}
