package com.medisalud.infrastructure.rest.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Franja horaria disponible")
public class TimeSlotResponse {
    
    @Schema(description = "Hora de inicio de la franja")
    private LocalDateTime startTime;
    
    @Schema(description = "Hora de fin de la franja")
    private LocalDateTime endTime;
    
    @Schema(description = "Indica si la franja está disponible")
    private boolean available;
}
