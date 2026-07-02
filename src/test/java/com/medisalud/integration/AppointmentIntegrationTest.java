package com.medisalud.integration;

import com.medisalud.domain.model.AppointmentStatus;
import com.medisalud.infrastructure.persistence.entity.AppointmentEntity;
import com.medisalud.infrastructure.persistence.entity.DoctorEntity;
import com.medisalud.infrastructure.persistence.entity.PatientEntity;
import com.medisalud.infrastructure.persistence.repository.R2dbcAppointmentRepository;
import com.medisalud.infrastructure.persistence.repository.R2dbcDoctorRepository;
import com.medisalud.infrastructure.persistence.repository.R2dbcPatientRepository;
import com.medisalud.infrastructure.rest.dto.AppointmentRequest;
import com.medisalud.infrastructure.rest.dto.AppointmentResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.web.reactive.server.WebTestClient;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import reactor.test.StepVerifier;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

@Testcontainers
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class AppointmentIntegrationTest {

    @Container
    static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:15-alpine")
            .withDatabaseName("medisalud_test")
            .withUsername("test")
            .withPassword("test");

    @DynamicPropertySource
    static void configureProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.r2dbc.url", () -> 
                String.format("r2dbc:postgresql://%s:%d/%s",
                        postgres.getHost(),
                        postgres.getFirstMappedPort(),
                        postgres.getDatabaseName()));
        registry.add("spring.r2dbc.username", postgres::getUsername);
        registry.add("spring.r2dbc.password", postgres::getPassword);
        registry.add("spring.flyway.url", postgres::getJdbcUrl);
        registry.add("spring.flyway.user", postgres::getUsername);
        registry.add("spring.flyway.password", postgres::getPassword);
        registry.add("medisalud.holidays.api.enabled", () -> "false");
        registry.add("medisalud.rate-limit.enabled", () -> "false");
    }

    @Autowired
    private WebTestClient webTestClient;

    @Autowired
    private R2dbcDoctorRepository doctorRepository;

    @Autowired
    private R2dbcPatientRepository patientRepository;

    @Autowired
    private R2dbcAppointmentRepository appointmentRepository;

    private UUID doctorId;
    private UUID patientId;

    @BeforeEach
    void setUp() {
        appointmentRepository.deleteAll().block();
        patientRepository.deleteAll().block();
        
        doctorId = UUID.randomUUID();
        DoctorEntity doctor = DoctorEntity.builder()
                .id(doctorId)
                .fullName("Dr. Test Integration")
                .specialty("Cardiología")
                .phone("5551234")
                .email("test.integration@medisalud.com")
                .build();
        
        doctorRepository.findById(doctorId)
                .switchIfEmpty(doctorRepository.save(doctor))
                .block();

        patientId = UUID.randomUUID();
        PatientEntity patient = PatientEntity.builder()
                .id(patientId)
                .fullName("Paciente Test")
                .documentId("INT-" + System.currentTimeMillis())
                .phone("5559999")
                .email("paciente.test@email.com")
                .birthDate(LocalDate.of(1990, 1, 1))
                .registrationDate(LocalDateTime.now())
                .build();
        patientRepository.save(patient).block();
    }

    @Test
    @DisplayName("Flujo completo: crear, consultar y cancelar cita")
    void shouldCompleteFullAppointmentFlow() {
        LocalDateTime futureDate = LocalDateTime.now().plusDays(7)
                .withHour(10).withMinute(0).withSecond(0).withNano(0);
        
        if (futureDate.getDayOfWeek() == java.time.DayOfWeek.SUNDAY) {
            futureDate = futureDate.plusDays(1);
        }
        if (futureDate.getDayOfWeek() == java.time.DayOfWeek.SATURDAY) {
            futureDate = futureDate.plusDays(2);
        }

        AppointmentRequest request = AppointmentRequest.builder()
                .patientId(patientId)
                .doctorId(doctorId)
                .dateTime(futureDate)
                .build();

        AppointmentResponse created = webTestClient.post()
                .uri("/api/v1/appointments")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(request)
                .exchange()
                .expectStatus().isCreated()
                .expectBody(AppointmentResponse.class)
                .returnResult()
                .getResponseBody();

        assertNotNull(created);
        assertEquals("PROGRAMADA", created.getStatus());
        UUID appointmentId = created.getId();

        webTestClient.get()
                .uri("/api/v1/appointments/{id}", appointmentId)
                .exchange()
                .expectStatus().isOk()
                .expectBody(AppointmentResponse.class)
                .value(response -> {
                    assertEquals(appointmentId, response.getId());
                    assertEquals(doctorId, response.getDoctorId());
                    assertEquals(patientId, response.getPatientId());
                });

        webTestClient.post()
                .uri("/api/v1/appointments/{id}/cancel", appointmentId)
                .exchange()
                .expectStatus().isOk()
                .expectBody(AppointmentResponse.class)
                .value(response -> {
                    assertEquals("CANCELADA", response.getStatus());
                });
    }

    @Test
    @DisplayName("Debe rechazar cita duplicada para el mismo médico y horario")
    void shouldRejectDuplicateAppointment() {
        LocalDateTime futureDate = LocalDateTime.now().plusDays(8)
                .withHour(11).withMinute(0).withSecond(0).withNano(0);
        
        if (futureDate.getDayOfWeek() == java.time.DayOfWeek.SUNDAY) {
            futureDate = futureDate.plusDays(1);
        }
        if (futureDate.getDayOfWeek() == java.time.DayOfWeek.SATURDAY) {
            futureDate = futureDate.plusDays(2);
        }

        AppointmentEntity existing = AppointmentEntity.builder()
                .id(UUID.randomUUID())
                .patientId(UUID.randomUUID())
                .doctorId(doctorId)
                .dateTime(futureDate)
                .status(AppointmentStatus.PROGRAMADA.name())
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();
        appointmentRepository.save(existing).block();

        UUID newPatientId = UUID.randomUUID();
        PatientEntity newPatient = PatientEntity.builder()
                .id(newPatientId)
                .fullName("Otro Paciente")
                .documentId("DUP-" + System.currentTimeMillis())
                .phone("5558888")
                .email("otro@email.com")
                .birthDate(LocalDate.of(1985, 5, 15))
                .registrationDate(LocalDateTime.now())
                .build();
        patientRepository.save(newPatient).block();

        AppointmentRequest request = AppointmentRequest.builder()
                .patientId(newPatientId)
                .doctorId(doctorId)
                .dateTime(futureDate)
                .build();

        webTestClient.post()
                .uri("/api/v1/appointments")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(request)
                .exchange()
                .expectStatus().is4xxClientError();
    }

    @Test
    @DisplayName("Debe consultar disponibilidad de horarios")
    void shouldGetAvailableSlots() {
        LocalDate futureDate = LocalDate.now().plusDays(10);
        
        if (futureDate.getDayOfWeek() == java.time.DayOfWeek.SUNDAY) {
            futureDate = futureDate.plusDays(1);
        }
        if (futureDate.getDayOfWeek() == java.time.DayOfWeek.SATURDAY) {
            futureDate = futureDate.plusDays(2);
        }

        webTestClient.get()
                .uri(uriBuilder -> uriBuilder
                        .path("/api/v1/appointments/available")
                        .queryParam("doctorId", doctorId)
                        .queryParam("startDate", futureDate)
                        .queryParam("endDate", futureDate)
                        .build())
                .exchange()
                .expectStatus().isOk()
                .expectBodyList(Object.class)
                .value(slots -> {
                    assertFalse(slots.isEmpty());
                });
    }

    @Test
    @DisplayName("Health check debe responder OK")
    void healthCheckShouldBeUp() {
        webTestClient.get()
                .uri("/actuator/health")
                .exchange()
                .expectStatus().isOk();
    }
}
