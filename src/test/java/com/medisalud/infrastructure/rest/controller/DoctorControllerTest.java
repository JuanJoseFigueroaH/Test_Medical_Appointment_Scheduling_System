package com.medisalud.infrastructure.rest.controller;

import com.medisalud.application.port.input.DoctorUseCase;
import com.medisalud.domain.model.Doctor;
import com.medisalud.infrastructure.rest.dto.DoctorRequest;
import com.medisalud.infrastructure.rest.dto.DoctorResponse;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.reactive.WebFluxTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.reactive.server.WebTestClient;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@WebFluxTest(DoctorController.class)
class DoctorControllerTest {

    @Autowired
    private WebTestClient webTestClient;

    @MockBean
    private DoctorUseCase doctorUseCase;

    @Test
    @DisplayName("POST /api/v1/doctors - Debe crear un médico exitosamente")
    void shouldCreateDoctorSuccessfully() {
        UUID doctorId = UUID.randomUUID();
        Doctor doctor = Doctor.builder()
                .id(doctorId)
                .fullName("Dr. Test")
                .specialty("Cardiología")
                .phone("5551234")
                .email("test@medisalud.com")
                .build();

        when(doctorUseCase.createDoctor(any(Doctor.class))).thenReturn(Mono.just(doctor));

        DoctorRequest request = DoctorRequest.builder()
                .fullName("Dr. Test")
                .specialty("Cardiología")
                .phone("5551234")
                .email("test@medisalud.com")
                .build();

        webTestClient.post()
                .uri("/api/v1/doctors")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(request)
                .exchange()
                .expectStatus().isCreated()
                .expectBody(DoctorResponse.class)
                .value(response -> {
                    assert response.getId().equals(doctorId);
                    assert response.getFullName().equals("Dr. Test");
                });
    }

    @Test
    @DisplayName("GET /api/v1/doctors - Debe listar todos los médicos")
    void shouldListAllDoctors() {
        Doctor doctor1 = Doctor.builder()
                .id(UUID.randomUUID())
                .fullName("Dr. Test 1")
                .specialty("Cardiología")
                .phone("5551234")
                .email("test1@medisalud.com")
                .build();

        Doctor doctor2 = Doctor.builder()
                .id(UUID.randomUUID())
                .fullName("Dr. Test 2")
                .specialty("Pediatría")
                .phone("5555678")
                .email("test2@medisalud.com")
                .build();

        when(doctorUseCase.getAllDoctors()).thenReturn(Flux.just(doctor1, doctor2));

        webTestClient.get()
                .uri("/api/v1/doctors")
                .exchange()
                .expectStatus().isOk()
                .expectBodyList(DoctorResponse.class)
                .hasSize(2);
    }

    @Test
    @DisplayName("GET /api/v1/doctors/{id} - Debe obtener un médico por ID")
    void shouldGetDoctorById() {
        UUID doctorId = UUID.randomUUID();
        Doctor doctor = Doctor.builder()
                .id(doctorId)
                .fullName("Dr. Test")
                .specialty("Cardiología")
                .phone("5551234")
                .email("test@medisalud.com")
                .build();

        when(doctorUseCase.getDoctorById(doctorId)).thenReturn(Mono.just(doctor));

        webTestClient.get()
                .uri("/api/v1/doctors/{id}", doctorId)
                .exchange()
                .expectStatus().isOk()
                .expectBody(DoctorResponse.class)
                .value(response -> {
                    assert response.getId().equals(doctorId);
                    assert response.getFullName().equals("Dr. Test");
                });
    }

    @Test
    @DisplayName("POST /api/v1/doctors - Debe fallar con datos inválidos")
    void shouldFailWithInvalidData() {
        DoctorRequest request = DoctorRequest.builder()
                .fullName("")
                .specialty("")
                .phone("123")
                .email("invalid-email")
                .build();

        webTestClient.post()
                .uri("/api/v1/doctors")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(request)
                .exchange()
                .expectStatus().isBadRequest();
    }

    @Test
    @DisplayName("DELETE /api/v1/doctors/{id} - Debe eliminar un médico")
    void shouldDeleteDoctor() {
        UUID doctorId = UUID.randomUUID();

        when(doctorUseCase.deleteDoctor(doctorId)).thenReturn(Mono.empty());

        webTestClient.delete()
                .uri("/api/v1/doctors/{id}", doctorId)
                .exchange()
                .expectStatus().isNoContent();
    }
}
