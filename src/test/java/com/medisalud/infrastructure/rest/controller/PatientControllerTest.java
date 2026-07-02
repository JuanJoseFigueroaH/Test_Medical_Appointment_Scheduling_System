package com.medisalud.infrastructure.rest.controller;

import com.medisalud.application.port.input.PatientUseCase;
import com.medisalud.domain.model.Patient;
import com.medisalud.infrastructure.rest.dto.PatientRequest;
import com.medisalud.infrastructure.rest.dto.PatientResponse;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.reactive.WebFluxTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.reactive.server.WebTestClient;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@WebFluxTest(PatientController.class)
class PatientControllerTest {

    @Autowired
    private WebTestClient webTestClient;

    @MockBean
    private PatientUseCase patientUseCase;

    @Test
    @DisplayName("POST /api/v1/patients - Debe crear un paciente exitosamente")
    void shouldCreatePatientSuccessfully() {
        UUID patientId = UUID.randomUUID();
        Patient patient = Patient.builder()
                .id(patientId)
                .fullName("Juan Pérez")
                .documentId("12345678")
                .phone("5551234")
                .email("juan@email.com")
                .birthDate(LocalDate.of(1990, 5, 15))
                .registrationDate(LocalDateTime.now())
                .build();

        when(patientUseCase.createPatient(any(Patient.class))).thenReturn(Mono.just(patient));

        PatientRequest request = PatientRequest.builder()
                .fullName("Juan Pérez")
                .documentId("12345678")
                .phone("5551234")
                .email("juan@email.com")
                .birthDate(LocalDate.of(1990, 5, 15))
                .build();

        webTestClient.post()
                .uri("/api/v1/patients")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(request)
                .exchange()
                .expectStatus().isCreated()
                .expectBody(PatientResponse.class)
                .value(response -> {
                    assert response.getId().equals(patientId);
                    assert response.getFullName().equals("Juan Pérez");
                });
    }

    @Test
    @DisplayName("GET /api/v1/patients - Debe listar todos los pacientes")
    void shouldListAllPatients() {
        Patient patient1 = Patient.builder()
                .id(UUID.randomUUID())
                .fullName("Juan Pérez")
                .documentId("12345678")
                .phone("5551234")
                .email("juan@email.com")
                .build();

        Patient patient2 = Patient.builder()
                .id(UUID.randomUUID())
                .fullName("María García")
                .documentId("87654321")
                .phone("5555678")
                .email("maria@email.com")
                .build();

        when(patientUseCase.getAllPatients()).thenReturn(Flux.just(patient1, patient2));

        webTestClient.get()
                .uri("/api/v1/patients")
                .exchange()
                .expectStatus().isOk()
                .expectBodyList(PatientResponse.class)
                .hasSize(2);
    }

    @Test
    @DisplayName("GET /api/v1/patients/{id} - Debe obtener un paciente por ID")
    void shouldGetPatientById() {
        UUID patientId = UUID.randomUUID();
        Patient patient = Patient.builder()
                .id(patientId)
                .fullName("Juan Pérez")
                .documentId("12345678")
                .phone("5551234")
                .email("juan@email.com")
                .build();

        when(patientUseCase.getPatientById(patientId)).thenReturn(Mono.just(patient));

        webTestClient.get()
                .uri("/api/v1/patients/{id}", patientId)
                .exchange()
                .expectStatus().isOk()
                .expectBody(PatientResponse.class)
                .value(response -> {
                    assert response.getId().equals(patientId);
                    assert response.getFullName().equals("Juan Pérez");
                });
    }

    @Test
    @DisplayName("POST /api/v1/patients - Debe fallar con fecha de nacimiento futura (RN-03)")
    void shouldFailWithFutureBirthDate() {
        PatientRequest request = PatientRequest.builder()
                .fullName("Juan Pérez")
                .documentId("12345678")
                .phone("5551234")
                .email("juan@email.com")
                .birthDate(LocalDate.now().plusDays(1))
                .build();

        webTestClient.post()
                .uri("/api/v1/patients")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(request)
                .exchange()
                .expectStatus().isBadRequest();
    }

    @Test
    @DisplayName("POST /api/v1/patients - Debe fallar con datos inválidos")
    void shouldFailWithInvalidData() {
        PatientRequest request = PatientRequest.builder()
                .fullName("")
                .documentId("123")
                .phone("123")
                .email("invalid-email")
                .build();

        webTestClient.post()
                .uri("/api/v1/patients")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(request)
                .exchange()
                .expectStatus().isBadRequest();
    }

    @Test
    @DisplayName("GET /api/v1/patients/document/{documentId} - Debe obtener paciente por documento")
    void shouldGetPatientByDocumentId() {
        String documentId = "12345678";
        Patient patient = Patient.builder()
                .id(UUID.randomUUID())
                .fullName("Juan Pérez")
                .documentId(documentId)
                .phone("5551234")
                .email("juan@email.com")
                .build();

        when(patientUseCase.getPatientByDocumentId(documentId)).thenReturn(Mono.just(patient));

        webTestClient.get()
                .uri("/api/v1/patients/document/{documentId}", documentId)
                .exchange()
                .expectStatus().isOk()
                .expectBody(PatientResponse.class)
                .value(response -> {
                    assert response.getDocumentId().equals(documentId);
                });
    }
}
