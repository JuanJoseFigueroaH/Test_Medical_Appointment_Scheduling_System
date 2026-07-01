package com.medisalud.infrastructure.rest.controller;

import com.medisalud.application.port.input.AppointmentUseCase;
import com.medisalud.domain.model.AppointmentStatus;
import com.medisalud.infrastructure.rest.dto.*;
import com.medisalud.infrastructure.rest.mapper.AppointmentDtoMapper;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/appointments")
@RequiredArgsConstructor
@Tag(name = "Citas", description = "API para gestión de citas médicas")
public class AppointmentController {
    
    private final AppointmentUseCase appointmentUseCase;
    private final AppointmentDtoMapper mapper;
    
    @PostMapping
    @Operation(summary = "Reservar cita", description = "Reserva una nueva cita médica")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Cita creada exitosamente",
                    content = @Content(schema = @Schema(implementation = AppointmentResponse.class))),
            @ApiResponse(responseCode = "400", description = "Datos de entrada inválidos o horario no válido. " +
                    "Códigos: INVALID_TIME_SLOT (horario fuera de atención), INVALID_SLOT_FORMAT (formato de franja inválido)",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "404", description = "Paciente o médico no encontrado. " +
                    "Códigos: PATIENT_NOT_FOUND, DOCTOR_NOT_FOUND",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "409", description = "Conflicto de horario o paciente penalizado. " +
                    "Códigos: APPOINTMENT_CONFLICT (médico ocupado), PATIENT_CONFLICT (paciente ya tiene cita), " +
                    "PATIENT_PENALIZED (paciente con 3+ penalizaciones)",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    public Mono<ResponseEntity<AppointmentResponse>> createAppointment(@Valid @RequestBody AppointmentRequest request) {
        return appointmentUseCase.createAppointment(request.patientId(), request.doctorId(), request.dateTime())
                .map(mapper::toResponse)
                .map(response -> ResponseEntity.status(HttpStatus.CREATED).body(response));
    }
    
    @GetMapping("/{id}")
    @Operation(summary = "Obtener cita por ID", description = "Obtiene los datos de una cita por su ID")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Cita encontrada",
                    content = @Content(schema = @Schema(implementation = AppointmentResponse.class))),
            @ApiResponse(responseCode = "404", description = "Cita no encontrada",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    public Mono<ResponseEntity<AppointmentResponse>> getAppointmentById(
            @Parameter(description = "ID de la cita") @PathVariable UUID id) {
        return appointmentUseCase.getAppointmentById(id)
                .map(mapper::toResponse)
                .map(ResponseEntity::ok);
    }
    
    @GetMapping("/available")
    @Operation(summary = "Consultar disponibilidad", description = "Consulta las franjas horarias disponibles de un médico")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Lista de franjas disponibles"),
            @ApiResponse(responseCode = "404", description = "Médico no encontrado",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    public Flux<TimeSlotResponse> getAvailableSlots(
            @Parameter(description = "ID del médico", required = true) @RequestParam UUID doctorId,
            @Parameter(description = "Fecha de inicio (YYYY-MM-DD)", required = true) 
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @Parameter(description = "Fecha de fin (YYYY-MM-DD)", required = true) 
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate) {
        return appointmentUseCase.getAvailableSlots(doctorId, startDate, endDate)
                .map(mapper::toTimeSlotResponse);
    }
    
    @PostMapping("/{id}/cancel")
    @Operation(summary = "Cancelar cita", description = "Cancela una cita programada. Aplica penalización si es tardía (<2 horas).")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Cita cancelada exitosamente",
                    content = @Content(schema = @Schema(implementation = AppointmentResponse.class))),
            @ApiResponse(responseCode = "400", description = "La cita no puede ser cancelada. " +
                    "Códigos: INVALID_STATUS (cita ya cancelada o atendida), PAST_APPOINTMENT (cita ya pasó)",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "404", description = "Cita no encontrada. Código: APPOINTMENT_NOT_FOUND",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    public Mono<ResponseEntity<AppointmentResponse>> cancelAppointment(
            @Parameter(description = "ID de la cita") @PathVariable UUID id) {
        return appointmentUseCase.cancelAppointment(id)
                .map(mapper::toResponse)
                .map(ResponseEntity::ok);
    }
    
    @PostMapping("/{id}/reschedule")
    @Operation(summary = "Reprogramar cita", description = "Reprograma una cita a un nuevo horario. Aplica penalización si es tardía (<2 horas).")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Cita reprogramada exitosamente",
                    content = @Content(schema = @Schema(implementation = AppointmentResponse.class))),
            @ApiResponse(responseCode = "400", description = "La cita no puede ser reprogramada o horario inválido. " +
                    "Códigos: INVALID_STATUS (cita no programada), PAST_APPOINTMENT (cita original ya pasó), " +
                    "INVALID_TIME_SLOT (nuevo horario fuera de atención)",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "404", description = "Cita no encontrada. Código: APPOINTMENT_NOT_FOUND",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "409", description = "Conflicto de horario. " +
                    "Códigos: APPOINTMENT_CONFLICT (médico ocupado), PATIENT_CONFLICT (paciente ya tiene cita)",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    public Mono<ResponseEntity<AppointmentResponse>> rescheduleAppointment(
            @Parameter(description = "ID de la cita") @PathVariable UUID id,
            @Valid @RequestBody RescheduleRequest request) {
        return appointmentUseCase.rescheduleAppointment(id, request.newDateTime())
                .map(mapper::toResponse)
                .map(ResponseEntity::ok);
    }
    
    @GetMapping
    @Operation(summary = "Listar citas", description = "Lista citas con filtros opcionales y paginación")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Lista de citas paginada"),
            @ApiResponse(responseCode = "400", description = "Parámetros de paginación inválidos",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    public Mono<PagedResponse<AppointmentResponse>> listAppointments(
            @Parameter(description = "Filtrar por ID del médico") @RequestParam(required = false) UUID doctorId,
            @Parameter(description = "Filtrar por ID del paciente") @RequestParam(required = false) UUID patientId,
            @Parameter(description = "Filtrar por estado") @RequestParam(required = false) AppointmentStatus status,
            @Parameter(description = "Fecha de inicio (YYYY-MM-DD)") 
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @Parameter(description = "Fecha de fin (YYYY-MM-DD)") 
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate,
            @Parameter(description = "Número de página (0-indexed)") @RequestParam(defaultValue = "0") @Min(0) int page,
            @Parameter(description = "Tamaño de página (máx 100)") @RequestParam(defaultValue = "20") @Min(1) @Max(100) int size) {
        int offset = page * size;
        
        Mono<List<AppointmentResponse>> contentMono = appointmentUseCase
                .listAppointmentsPaginated(doctorId, patientId, status, startDate, endDate, size, offset)
                .map(mapper::toResponse)
                .collectList();
        
        Mono<Long> countMono = appointmentUseCase.countAppointments(doctorId, patientId, status, startDate, endDate);
        
        return Mono.zip(contentMono, countMono)
                .map(tuple -> PagedResponse.of(tuple.getT1(), page, size, tuple.getT2()));
    }
    
    @PostMapping("/{id}/attend")
    @Operation(summary = "Marcar como atendida", description = "Marca una cita como atendida")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Cita marcada como atendida",
                    content = @Content(schema = @Schema(implementation = AppointmentResponse.class))),
            @ApiResponse(responseCode = "400", description = "La cita no puede ser marcada como atendida",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "404", description = "Cita no encontrada",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    public Mono<ResponseEntity<AppointmentResponse>> markAsAttended(
            @Parameter(description = "ID de la cita") @PathVariable UUID id) {
        return appointmentUseCase.markAsAttended(id)
                .map(mapper::toResponse)
                .map(ResponseEntity::ok);
    }
}
