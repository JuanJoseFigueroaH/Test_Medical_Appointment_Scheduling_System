package com.medisalud.infrastructure.rest.controller;

import com.medisalud.application.port.input.AppointmentUseCase;
import com.medisalud.domain.model.Appointment;
import com.medisalud.domain.model.AppointmentStatus;
import com.medisalud.domain.model.TimeSlot;
import com.medisalud.infrastructure.rest.dto.AppointmentRequest;
import com.medisalud.infrastructure.rest.dto.AppointmentResponse;
import com.medisalud.infrastructure.rest.dto.PagedResponse;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.reactive.WebFluxTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.MediaType;
import org.springframework.test.web.reactive.server.WebTestClient;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

@WebFluxTest(AppointmentController.class)
class AppointmentControllerTest {

    @Autowired
    private WebTestClient webTestClient;

    @MockBean
    private AppointmentUseCase appointmentUseCase;

    @Test
    @DisplayName("POST /api/v1/appointments - Debe crear una cita exitosamente")
    void shouldCreateAppointmentSuccessfully() {
        UUID appointmentId = UUID.randomUUID();
        UUID patientId = UUID.randomUUID();
        UUID doctorId = UUID.randomUUID();
        LocalDateTime dateTime = LocalDateTime.of(2025, 12, 16, 10, 0);

        Appointment appointment = Appointment.builder()
                .id(appointmentId)
                .patientId(patientId)
                .doctorId(doctorId)
                .dateTime(dateTime)
                .status(AppointmentStatus.PROGRAMADA)
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();

        when(appointmentUseCase.createAppointment(eq(patientId), eq(doctorId), eq(dateTime)))
                .thenReturn(Mono.just(appointment));

        AppointmentRequest request = AppointmentRequest.builder()
                .patientId(patientId)
                .doctorId(doctorId)
                .dateTime(dateTime)
                .build();

        webTestClient.post()
                .uri("/api/v1/appointments")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(request)
                .exchange()
                .expectStatus().isCreated()
                .expectBody(AppointmentResponse.class)
                .value(response -> {
                    assert response.getId().equals(appointmentId);
                    assert response.getStatus().equals("PROGRAMADA");
                });
    }

    @Test
    @DisplayName("GET /api/v1/appointments/{id} - Debe obtener una cita por ID")
    void shouldGetAppointmentById() {
        UUID appointmentId = UUID.randomUUID();
        Appointment appointment = Appointment.builder()
                .id(appointmentId)
                .patientId(UUID.randomUUID())
                .doctorId(UUID.randomUUID())
                .dateTime(LocalDateTime.of(2025, 12, 16, 10, 0))
                .status(AppointmentStatus.PROGRAMADA)
                .build();

        when(appointmentUseCase.getAppointmentById(appointmentId)).thenReturn(Mono.just(appointment));

        webTestClient.get()
                .uri("/api/v1/appointments/{id}", appointmentId)
                .exchange()
                .expectStatus().isOk()
                .expectBody(AppointmentResponse.class)
                .value(response -> {
                    assert response.getId().equals(appointmentId);
                });
    }

    @Test
    @DisplayName("GET /api/v1/appointments/available - Debe obtener franjas disponibles")
    void shouldGetAvailableSlots() {
        UUID doctorId = UUID.randomUUID();
        LocalDate date = LocalDate.of(2025, 12, 16);

        TimeSlot slot1 = TimeSlot.builder()
                .startTime(LocalDateTime.of(2025, 12, 16, 8, 0))
                .endTime(LocalDateTime.of(2025, 12, 16, 8, 30))
                .available(true)
                .build();

        TimeSlot slot2 = TimeSlot.builder()
                .startTime(LocalDateTime.of(2025, 12, 16, 8, 30))
                .endTime(LocalDateTime.of(2025, 12, 16, 9, 0))
                .available(true)
                .build();

        when(appointmentUseCase.getAvailableSlots(eq(doctorId), eq(date), eq(date)))
                .thenReturn(Flux.just(slot1, slot2));

        webTestClient.get()
                .uri(uriBuilder -> uriBuilder
                        .path("/api/v1/appointments/available")
                        .queryParam("doctorId", doctorId)
                        .queryParam("startDate", date)
                        .queryParam("endDate", date)
                        .build())
                .exchange()
                .expectStatus().isOk()
                .expectBodyList(Object.class)
                .hasSize(2);
    }

    @Test
    @DisplayName("POST /api/v1/appointments/{id}/cancel - Debe cancelar una cita")
    void shouldCancelAppointment() {
        UUID appointmentId = UUID.randomUUID();
        Appointment cancelledAppointment = Appointment.builder()
                .id(appointmentId)
                .patientId(UUID.randomUUID())
                .doctorId(UUID.randomUUID())
                .dateTime(LocalDateTime.of(2025, 12, 16, 10, 0))
                .status(AppointmentStatus.CANCELADA)
                .cancellationDateTime(LocalDateTime.now())
                .build();

        when(appointmentUseCase.cancelAppointment(appointmentId))
                .thenReturn(Mono.just(cancelledAppointment));

        webTestClient.post()
                .uri("/api/v1/appointments/{id}/cancel", appointmentId)
                .exchange()
                .expectStatus().isOk()
                .expectBody(AppointmentResponse.class)
                .value(response -> {
                    assert response.getStatus().equals("CANCELADA");
                });
    }

    @Test
    @DisplayName("POST /api/v1/appointments/{id}/attend - Debe marcar cita como atendida")
    void shouldMarkAppointmentAsAttended() {
        UUID appointmentId = UUID.randomUUID();
        Appointment attendedAppointment = Appointment.builder()
                .id(appointmentId)
                .patientId(UUID.randomUUID())
                .doctorId(UUID.randomUUID())
                .dateTime(LocalDateTime.of(2025, 12, 16, 10, 0))
                .status(AppointmentStatus.ATENDIDA)
                .build();

        when(appointmentUseCase.markAsAttended(appointmentId))
                .thenReturn(Mono.just(attendedAppointment));

        webTestClient.post()
                .uri("/api/v1/appointments/{id}/attend", appointmentId)
                .exchange()
                .expectStatus().isOk()
                .expectBody(AppointmentResponse.class)
                .value(response -> {
                    assert response.getStatus().equals("ATENDIDA");
                });
    }

    @Test
    @DisplayName("GET /api/v1/appointments - Debe listar citas con filtros y paginación")
    void shouldListAppointmentsWithFilters() {
        UUID doctorId = UUID.randomUUID();
        Appointment appointment = Appointment.builder()
                .id(UUID.randomUUID())
                .patientId(UUID.randomUUID())
                .doctorId(doctorId)
                .dateTime(LocalDateTime.of(2025, 12, 16, 10, 0))
                .status(AppointmentStatus.PROGRAMADA)
                .build();

        when(appointmentUseCase.listAppointmentsPaginated(eq(doctorId), any(), any(), any(), any(), anyInt(), anyLong()))
                .thenReturn(Flux.just(appointment));
        when(appointmentUseCase.countAppointments(eq(doctorId), any(), any(), any(), any()))
                .thenReturn(Mono.just(1L));

        webTestClient.get()
                .uri(uriBuilder -> uriBuilder
                        .path("/api/v1/appointments")
                        .queryParam("doctorId", doctorId)
                        .build())
                .exchange()
                .expectStatus().isOk()
                .expectBody(new ParameterizedTypeReference<PagedResponse<AppointmentResponse>>() {})
                .value(response -> {
                    assertNotNull(response);
                    assertEquals(1, response.getContent().size());
                    assertEquals(1L, response.getTotalElements());
                    assertEquals(0, response.getPage());
                    assertFalse(response.isHasNext());
                });
    }
}
