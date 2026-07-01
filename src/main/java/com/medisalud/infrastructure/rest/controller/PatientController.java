package com.medisalud.infrastructure.rest.controller;

import com.medisalud.application.port.input.PatientUseCase;
import com.medisalud.infrastructure.rest.dto.ErrorResponse;
import com.medisalud.infrastructure.rest.dto.PatientRequest;
import com.medisalud.infrastructure.rest.dto.PatientResponse;
import com.medisalud.infrastructure.rest.mapper.PatientDtoMapper;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.UUID;

@RestController
@RequestMapping("/api/v1/patients")
@RequiredArgsConstructor
@Tag(name = "Pacientes", description = "API para gestión de pacientes")
public class PatientController {
    
    private final PatientUseCase patientUseCase;
    private final PatientDtoMapper mapper;
    
    @PostMapping
    @Operation(summary = "Registrar paciente", description = "Registra un nuevo paciente en el sistema")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Paciente creado exitosamente",
                    content = @Content(schema = @Schema(implementation = PatientResponse.class))),
            @ApiResponse(responseCode = "400", description = "Datos de entrada inválidos",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "409", description = "Ya existe un paciente con ese documento de identidad",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    public Mono<ResponseEntity<PatientResponse>> createPatient(@Valid @RequestBody PatientRequest request) {
        return patientUseCase.createPatient(mapper.toDomain(request))
                .map(mapper::toResponse)
                .map(response -> ResponseEntity.status(HttpStatus.CREATED).body(response));
    }
    
    @GetMapping("/{id}")
    @Operation(summary = "Obtener paciente por ID", description = "Obtiene los datos de un paciente por su ID")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Paciente encontrado",
                    content = @Content(schema = @Schema(implementation = PatientResponse.class))),
            @ApiResponse(responseCode = "404", description = "Paciente no encontrado",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    public Mono<ResponseEntity<PatientResponse>> getPatientById(
            @Parameter(description = "ID del paciente") @PathVariable UUID id) {
        return patientUseCase.getPatientById(id)
                .map(mapper::toResponse)
                .map(ResponseEntity::ok);
    }
    
    @GetMapping("/document/{documentId}")
    @Operation(summary = "Obtener paciente por documento", description = "Obtiene los datos de un paciente por su documento de identidad")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Paciente encontrado",
                    content = @Content(schema = @Schema(implementation = PatientResponse.class))),
            @ApiResponse(responseCode = "404", description = "Paciente no encontrado",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    public Mono<ResponseEntity<PatientResponse>> getPatientByDocumentId(
            @Parameter(description = "Documento de identidad") @PathVariable String documentId) {
        return patientUseCase.getPatientByDocumentId(documentId)
                .map(mapper::toResponse)
                .map(ResponseEntity::ok);
    }
    
    @GetMapping
    @Operation(summary = "Listar pacientes", description = "Obtiene la lista de pacientes con paginación opcional")
    @ApiResponse(responseCode = "200", description = "Lista de pacientes paginada")
    public Flux<PatientResponse> getAllPatients(
            @Parameter(description = "Número de página (0-indexed)") @RequestParam(defaultValue = "0") int page,
            @Parameter(description = "Tamaño de página") @RequestParam(defaultValue = "20") int size) {
        if (size > 100) size = 100;
        if (size < 1) size = 20;
        if (page < 0) page = 0;
        return patientUseCase.getAllPatientsPaginated(page, size)
                .map(mapper::toResponse);
    }
    
    @PutMapping("/{id}")
    @Operation(summary = "Actualizar paciente", description = "Actualiza los datos de un paciente existente")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Paciente actualizado exitosamente",
                    content = @Content(schema = @Schema(implementation = PatientResponse.class))),
            @ApiResponse(responseCode = "400", description = "Datos de entrada inválidos",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "404", description = "Paciente no encontrado",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "409", description = "Ya existe un paciente con ese documento de identidad",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    public Mono<ResponseEntity<PatientResponse>> updatePatient(
            @Parameter(description = "ID del paciente") @PathVariable UUID id,
            @Valid @RequestBody PatientRequest request) {
        return patientUseCase.updatePatient(id, mapper.toDomain(request))
                .map(mapper::toResponse)
                .map(ResponseEntity::ok);
    }
    
    @DeleteMapping("/{id}")
    @Operation(summary = "Eliminar paciente", description = "Elimina un paciente del sistema")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "204", description = "Paciente eliminado exitosamente"),
            @ApiResponse(responseCode = "404", description = "Paciente no encontrado",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    public Mono<ResponseEntity<Void>> deletePatient(
            @Parameter(description = "ID del paciente") @PathVariable UUID id) {
        return patientUseCase.deletePatient(id)
                .then(Mono.just(ResponseEntity.noContent().<Void>build()));
    }
}
