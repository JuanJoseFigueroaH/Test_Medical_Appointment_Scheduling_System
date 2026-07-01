package com.medisalud.infrastructure.rest.controller;

import com.medisalud.application.port.input.DoctorUseCase;
import com.medisalud.infrastructure.rest.dto.DoctorRequest;
import com.medisalud.infrastructure.rest.dto.DoctorResponse;
import com.medisalud.infrastructure.rest.dto.ErrorResponse;
import com.medisalud.infrastructure.rest.mapper.DoctorDtoMapper;
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
@RequestMapping("/api/v1/doctors")
@RequiredArgsConstructor
@Tag(name = "Médicos", description = "API para gestión de médicos")
public class DoctorController {
    
    private final DoctorUseCase doctorUseCase;
    private final DoctorDtoMapper mapper;
    
    @PostMapping
    @Operation(summary = "Registrar médico", description = "Registra un nuevo médico en el sistema")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Médico creado exitosamente",
                    content = @Content(schema = @Schema(implementation = DoctorResponse.class))),
            @ApiResponse(responseCode = "400", description = "Datos de entrada inválidos",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    public Mono<ResponseEntity<DoctorResponse>> createDoctor(@Valid @RequestBody DoctorRequest request) {
        return doctorUseCase.createDoctor(mapper.toDomain(request))
                .map(mapper::toResponse)
                .map(response -> ResponseEntity.status(HttpStatus.CREATED).body(response));
    }
    
    @GetMapping("/{id}")
    @Operation(summary = "Obtener médico por ID", description = "Obtiene los datos de un médico por su ID")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Médico encontrado",
                    content = @Content(schema = @Schema(implementation = DoctorResponse.class))),
            @ApiResponse(responseCode = "404", description = "Médico no encontrado",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    public Mono<ResponseEntity<DoctorResponse>> getDoctorById(
            @Parameter(description = "ID del médico") @PathVariable UUID id) {
        return doctorUseCase.getDoctorById(id)
                .map(mapper::toResponse)
                .map(ResponseEntity::ok);
    }
    
    @GetMapping
    @Operation(summary = "Listar médicos", description = "Obtiene la lista de médicos con paginación opcional")
    @ApiResponse(responseCode = "200", description = "Lista de médicos paginada")
    public Flux<DoctorResponse> getAllDoctors(
            @Parameter(description = "Número de página (0-indexed)") @RequestParam(defaultValue = "0") int page,
            @Parameter(description = "Tamaño de página") @RequestParam(defaultValue = "20") int size) {
        if (size > 100) size = 100;
        if (size < 1) size = 20;
        if (page < 0) page = 0;
        return doctorUseCase.getAllDoctorsPaginated(page, size)
                .map(mapper::toResponse);
    }
    
    @PutMapping("/{id}")
    @Operation(summary = "Actualizar médico", description = "Actualiza los datos de un médico existente")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Médico actualizado exitosamente",
                    content = @Content(schema = @Schema(implementation = DoctorResponse.class))),
            @ApiResponse(responseCode = "400", description = "Datos de entrada inválidos",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "404", description = "Médico no encontrado",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    public Mono<ResponseEntity<DoctorResponse>> updateDoctor(
            @Parameter(description = "ID del médico") @PathVariable UUID id,
            @Valid @RequestBody DoctorRequest request) {
        return doctorUseCase.updateDoctor(id, mapper.toDomain(request))
                .map(mapper::toResponse)
                .map(ResponseEntity::ok);
    }
    
    @DeleteMapping("/{id}")
    @Operation(summary = "Eliminar médico", description = "Elimina un médico del sistema")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "204", description = "Médico eliminado exitosamente"),
            @ApiResponse(responseCode = "404", description = "Médico no encontrado",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    public Mono<ResponseEntity<Void>> deleteDoctor(
            @Parameter(description = "ID del médico") @PathVariable UUID id) {
        return doctorUseCase.deleteDoctor(id)
                .then(Mono.just(ResponseEntity.noContent().<Void>build()));
    }
}
