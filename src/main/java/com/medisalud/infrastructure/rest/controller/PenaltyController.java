package com.medisalud.infrastructure.rest.controller;

import com.medisalud.application.port.input.PenaltyUseCase;
import com.medisalud.domain.BusinessConstants;
import com.medisalud.infrastructure.rest.dto.ErrorResponse;
import com.medisalud.infrastructure.rest.dto.PatientPenaltySummary;
import com.medisalud.infrastructure.rest.dto.PenaltyResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Mono;

import java.util.UUID;

@RestController
@RequestMapping("/api/v1/patients/{patientId}/penalties")
@RequiredArgsConstructor
@Tag(name = "Penalizaciones", description = "API para consultar penalizaciones de pacientes")
public class PenaltyController {
    
    private final PenaltyUseCase penaltyUseCase;
    
    @GetMapping
    @Operation(summary = "Consultar penalizaciones", 
               description = "Obtiene el resumen de penalizaciones activas de un paciente en los últimos 30 días")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Resumen de penalizaciones",
                    content = @Content(schema = @Schema(implementation = PatientPenaltySummary.class))),
            @ApiResponse(responseCode = "404", description = "Paciente no encontrado",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    public Mono<ResponseEntity<PatientPenaltySummary>> getPatientPenalties(
            @Parameter(description = "ID del paciente") @PathVariable UUID patientId) {
        
        return penaltyUseCase.getPatientPenalties(patientId)
                .map(penalty -> PenaltyResponse.builder()
                        .id(penalty.getId())
                        .patientId(penalty.getPatientId())
                        .appointmentId(penalty.getAppointmentId())
                        .penaltyDateTime(penalty.getPenaltyDateTime())
                        .reason(penalty.getReason())
                        .build())
                .collectList()
                .zipWith(penaltyUseCase.getPatientPenaltyCount(patientId))
                .map(tuple -> {
                    var penalties = tuple.getT1();
                    var count = tuple.getT2();
                    return PatientPenaltySummary.builder()
                            .patientId(patientId)
                            .activePenaltyCount(count)
                            .blocked(count >= BusinessConstants.MAX_PENALTIES_BEFORE_BLOCK)
                            .penalties(penalties)
                            .build();
                })
                .map(ResponseEntity::ok);
    }
    
    @GetMapping("/count")
    @Operation(summary = "Contar penalizaciones", 
               description = "Obtiene el número de penalizaciones activas de un paciente")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Número de penalizaciones activas")
    })
    public Mono<ResponseEntity<Long>> getPatientPenaltyCount(
            @Parameter(description = "ID del paciente") @PathVariable UUID patientId) {
        return penaltyUseCase.getPatientPenaltyCount(patientId)
                .map(ResponseEntity::ok);
    }
}
