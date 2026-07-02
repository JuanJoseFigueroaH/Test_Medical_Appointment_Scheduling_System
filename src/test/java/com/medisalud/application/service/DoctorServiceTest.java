package com.medisalud.application.service;

import com.medisalud.application.port.output.DoctorRepository;
import com.medisalud.domain.exception.ResourceNotFoundException;
import com.medisalud.domain.model.Doctor;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class DoctorServiceTest {
    
    @Mock
    private DoctorRepository doctorRepository;
    
    private DoctorService doctorService;
    
    @BeforeEach
    void setUp() {
        doctorService = new DoctorService(doctorRepository);
    }
    
    @Nested
    @DisplayName("RF-01: Registro de Médicos")
    class CreateDoctorTests {
        
        @Test
        @DisplayName("Debe crear un médico exitosamente")
        void shouldCreateDoctorSuccessfully() {
            Doctor doctor = Doctor.builder()
                    .fullName("Dra. María González")
                    .specialty("Cardiología")
                    .phone("5551001")
                    .email("maria.gonzalez@medisalud.com")
                    .build();
            
            Doctor savedDoctor = Doctor.builder()
                    .id(UUID.randomUUID())
                    .fullName("Dra. María González")
                    .specialty("Cardiología")
                    .phone("5551001")
                    .email("maria.gonzalez@medisalud.com")
                    .build();
            
            when(doctorRepository.save(any(Doctor.class))).thenReturn(Mono.just(savedDoctor));
            
            StepVerifier.create(doctorService.createDoctor(doctor))
                    .expectNextMatches(d -> d.getId() != null && 
                            d.getFullName().equals("Dra. María González"))
                    .verifyComplete();
            
            verify(doctorRepository, times(1)).save(any(Doctor.class));
        }
    }
    
    @Nested
    @DisplayName("Consulta de Médicos")
    class GetDoctorTests {
        
        @Test
        @DisplayName("Debe obtener un médico por ID")
        void shouldGetDoctorById() {
            UUID doctorId = UUID.randomUUID();
            Doctor doctor = Doctor.builder()
                    .id(doctorId)
                    .fullName("Dr. Carlos Ruiz")
                    .specialty("Pediatría")
                    .build();
            
            when(doctorRepository.findById(doctorId)).thenReturn(Mono.just(doctor));
            
            StepVerifier.create(doctorService.getDoctorById(doctorId))
                    .expectNextMatches(d -> d.getId().equals(doctorId))
                    .verifyComplete();
        }
        
        @Test
        @DisplayName("Debe lanzar error si el médico no existe")
        void shouldThrowErrorIfDoctorNotFound() {
            UUID doctorId = UUID.randomUUID();
            
            when(doctorRepository.findById(doctorId)).thenReturn(Mono.empty());
            
            StepVerifier.create(doctorService.getDoctorById(doctorId))
                    .expectError(ResourceNotFoundException.class)
                    .verify();
        }
        
        @Test
        @DisplayName("Debe listar todos los médicos")
        void shouldListAllDoctors() {
            Doctor doctor1 = Doctor.builder().id(UUID.randomUUID()).fullName("Doctor 1").build();
            Doctor doctor2 = Doctor.builder().id(UUID.randomUUID()).fullName("Doctor 2").build();
            
            when(doctorRepository.findAll()).thenReturn(Flux.just(doctor1, doctor2));
            
            StepVerifier.create(doctorService.getAllDoctors())
                    .expectNextCount(2)
                    .verifyComplete();
        }
    }
    
    @Nested
    @DisplayName("Actualización de Médicos")
    class UpdateDoctorTests {
        
        @Test
        @DisplayName("Debe actualizar un médico exitosamente")
        void shouldUpdateDoctorSuccessfully() {
            UUID doctorId = UUID.randomUUID();
            Doctor existingDoctor = Doctor.builder()
                    .id(doctorId)
                    .fullName("Dr. Carlos Ruiz")
                    .specialty("Pediatría")
                    .build();
            
            Doctor updateData = Doctor.builder()
                    .fullName("Dr. Carlos Ruiz Actualizado")
                    .specialty("Pediatría General")
                    .phone("5559999")
                    .build();
            
            Doctor updatedDoctor = Doctor.builder()
                    .id(doctorId)
                    .fullName("Dr. Carlos Ruiz Actualizado")
                    .specialty("Pediatría General")
                    .phone("5559999")
                    .build();
            
            when(doctorRepository.findById(doctorId)).thenReturn(Mono.just(existingDoctor));
            when(doctorRepository.update(any(Doctor.class))).thenReturn(Mono.just(updatedDoctor));
            
            StepVerifier.create(doctorService.updateDoctor(doctorId, updateData))
                    .expectNextMatches(d -> d.getFullName().equals("Dr. Carlos Ruiz Actualizado"))
                    .verifyComplete();
        }
    }
    
    @Nested
    @DisplayName("Eliminación de Médicos")
    class DeleteDoctorTests {
        
        @Test
        @DisplayName("Debe eliminar un médico exitosamente")
        void shouldDeleteDoctorSuccessfully() {
            UUID doctorId = UUID.randomUUID();
            Doctor doctor = Doctor.builder().id(doctorId).fullName("Doctor").build();
            
            when(doctorRepository.findById(doctorId)).thenReturn(Mono.just(doctor));
            when(doctorRepository.deleteById(doctorId)).thenReturn(Mono.empty());
            
            StepVerifier.create(doctorService.deleteDoctor(doctorId))
                    .verifyComplete();
            
            verify(doctorRepository, times(1)).deleteById(doctorId);
        }
        
        @Test
        @DisplayName("Debe lanzar error al eliminar médico inexistente")
        void shouldThrowErrorWhenDeletingNonExistentDoctor() {
            UUID doctorId = UUID.randomUUID();
            
            when(doctorRepository.findById(doctorId)).thenReturn(Mono.empty());
            
            StepVerifier.create(doctorService.deleteDoctor(doctorId))
                    .expectError(ResourceNotFoundException.class)
                    .verify();
        }
    }
}
