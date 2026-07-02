package com.medisalud.application.service;

import com.medisalud.application.port.output.PatientRepository;
import com.medisalud.domain.exception.DuplicateResourceException;
import com.medisalud.domain.exception.ResourceNotFoundException;
import com.medisalud.domain.model.Patient;
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

import java.time.LocalDate;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class PatientServiceTest {
    
    @Mock
    private PatientRepository patientRepository;
    
    private PatientService patientService;
    
    @BeforeEach
    void setUp() {
        patientService = new PatientService(patientRepository);
    }
    
    @Nested
    @DisplayName("RN-03: Validación de Fecha de Nacimiento")
    class BirthDateValidationTests {
        
        @Test
        @DisplayName("RN-03: Debe rechazar fecha de nacimiento futura")
        void shouldRejectFutureBirthDate() {
            LocalDate futureBirthDate = LocalDate.now().plusDays(1);
            
            org.junit.jupiter.api.Assertions.assertThrows(IllegalArgumentException.class, () -> {
                Patient.builder()
                        .fullName("Juan Pérez García")
                        .documentId("12345678")
                        .phone("5552001")
                        .email("juan.perez@email.com")
                        .birthDate(futureBirthDate)
                        .build();
            });
        }
        
        @Test
        @DisplayName("RN-03: Debe aceptar fecha de nacimiento pasada")
        void shouldAcceptPastBirthDate() {
            LocalDate pastBirthDate = LocalDate.of(1990, 5, 15);
            
            Patient patient = Patient.builder()
                    .fullName("Juan Pérez García")
                    .documentId("12345678")
                    .phone("5552001")
                    .email("juan.perez@email.com")
                    .birthDate(pastBirthDate)
                    .build();
            
            org.junit.jupiter.api.Assertions.assertEquals(pastBirthDate, patient.getBirthDate());
        }
        
        @Test
        @DisplayName("RN-03: Debe aceptar fecha de nacimiento nula (opcional)")
        void shouldAcceptNullBirthDate() {
            Patient patient = Patient.builder()
                    .fullName("Juan Pérez García")
                    .documentId("12345678")
                    .phone("5552001")
                    .email("juan.perez@email.com")
                    .birthDate(null)
                    .build();
            
            org.junit.jupiter.api.Assertions.assertNull(patient.getBirthDate());
        }
    }
    
    @Nested
    @DisplayName("RF-02: Registro de Pacientes")
    class CreatePatientTests {
        
        @Test
        @DisplayName("Debe crear un paciente exitosamente")
        void shouldCreatePatientSuccessfully() {
            Patient patient = Patient.builder()
                    .fullName("Juan Pérez García")
                    .documentId("12345678")
                    .phone("5552001")
                    .email("juan.perez@email.com")
                    .birthDate(LocalDate.of(1990, 5, 15))
                    .build();
            
            Patient savedPatient = Patient.builder()
                    .id(UUID.randomUUID())
                    .fullName("Juan Pérez García")
                    .documentId("12345678")
                    .phone("5552001")
                    .email("juan.perez@email.com")
                    .birthDate(LocalDate.of(1990, 5, 15))
                    .build();
            
            when(patientRepository.existsByDocumentId("12345678")).thenReturn(Mono.just(false));
            when(patientRepository.save(any(Patient.class))).thenReturn(Mono.just(savedPatient));
            
            StepVerifier.create(patientService.createPatient(patient))
                    .expectNextMatches(p -> p.getId() != null && 
                            p.getDocumentId().equals("12345678"))
                    .verifyComplete();
        }
        
        @Test
        @DisplayName("Debe rechazar paciente con documento duplicado")
        void shouldRejectDuplicateDocumentId() {
            Patient patient = Patient.builder()
                    .fullName("Juan Pérez García")
                    .documentId("12345678")
                    .phone("5552001")
                    .email("juan.perez@email.com")
                    .build();
            
            when(patientRepository.existsByDocumentId("12345678")).thenReturn(Mono.just(true));
            
            StepVerifier.create(patientService.createPatient(patient))
                    .expectError(DuplicateResourceException.class)
                    .verify();
            
            verify(patientRepository, never()).save(any(Patient.class));
        }
    }
    
    @Nested
    @DisplayName("Consulta de Pacientes")
    class GetPatientTests {
        
        @Test
        @DisplayName("Debe obtener un paciente por ID")
        void shouldGetPatientById() {
            UUID patientId = UUID.randomUUID();
            Patient patient = Patient.builder()
                    .id(patientId)
                    .fullName("Juan Pérez")
                    .documentId("12345678")
                    .build();
            
            when(patientRepository.findById(patientId)).thenReturn(Mono.just(patient));
            
            StepVerifier.create(patientService.getPatientById(patientId))
                    .expectNextMatches(p -> p.getId().equals(patientId))
                    .verifyComplete();
        }
        
        @Test
        @DisplayName("Debe obtener un paciente por documento de identidad")
        void shouldGetPatientByDocumentId() {
            String documentId = "12345678";
            Patient patient = Patient.builder()
                    .id(UUID.randomUUID())
                    .fullName("Juan Pérez")
                    .documentId(documentId)
                    .build();
            
            when(patientRepository.findByDocumentId(documentId)).thenReturn(Mono.just(patient));
            
            StepVerifier.create(patientService.getPatientByDocumentId(documentId))
                    .expectNextMatches(p -> p.getDocumentId().equals(documentId))
                    .verifyComplete();
        }
        
        @Test
        @DisplayName("Debe lanzar error si el paciente no existe")
        void shouldThrowErrorIfPatientNotFound() {
            UUID patientId = UUID.randomUUID();
            
            when(patientRepository.findById(patientId)).thenReturn(Mono.empty());
            
            StepVerifier.create(patientService.getPatientById(patientId))
                    .expectError(ResourceNotFoundException.class)
                    .verify();
        }
        
        @Test
        @DisplayName("Debe listar todos los pacientes")
        void shouldListAllPatients() {
            Patient patient1 = Patient.builder().id(UUID.randomUUID()).fullName("Paciente 1").build();
            Patient patient2 = Patient.builder().id(UUID.randomUUID()).fullName("Paciente 2").build();
            
            when(patientRepository.findAll()).thenReturn(Flux.just(patient1, patient2));
            
            StepVerifier.create(patientService.getAllPatients())
                    .expectNextCount(2)
                    .verifyComplete();
        }
    }
    
    @Nested
    @DisplayName("Actualización de Pacientes")
    class UpdatePatientTests {
        
        @Test
        @DisplayName("Debe actualizar un paciente exitosamente")
        void shouldUpdatePatientSuccessfully() {
            UUID patientId = UUID.randomUUID();
            Patient existingPatient = Patient.builder()
                    .id(patientId)
                    .fullName("Juan Pérez")
                    .documentId("12345678")
                    .phone("5552001")
                    .email("juan@email.com")
                    .build();
            
            Patient updateData = Patient.builder()
                    .fullName("Juan Pérez Actualizado")
                    .documentId("12345678")
                    .phone("5559999")
                    .email("juan.nuevo@email.com")
                    .build();
            
            Patient updatedPatient = Patient.builder()
                    .id(patientId)
                    .fullName("Juan Pérez Actualizado")
                    .documentId("12345678")
                    .phone("5559999")
                    .email("juan.nuevo@email.com")
                    .build();
            
            when(patientRepository.findById(patientId)).thenReturn(Mono.just(existingPatient));
            when(patientRepository.update(any(Patient.class))).thenReturn(Mono.just(updatedPatient));
            
            StepVerifier.create(patientService.updatePatient(patientId, updateData))
                    .expectNextMatches(p -> p.getFullName().equals("Juan Pérez Actualizado"))
                    .verifyComplete();
        }
        
        @Test
        @DisplayName("Debe rechazar actualización con documento duplicado")
        void shouldRejectUpdateWithDuplicateDocument() {
            UUID patientId = UUID.randomUUID();
            Patient existingPatient = Patient.builder()
                    .id(patientId)
                    .fullName("Juan Pérez")
                    .documentId("12345678")
                    .build();
            
            Patient updateData = Patient.builder()
                    .fullName("Juan Pérez")
                    .documentId("87654321")
                    .phone("5552001")
                    .email("juan@email.com")
                    .build();
            
            when(patientRepository.findById(patientId)).thenReturn(Mono.just(existingPatient));
            when(patientRepository.existsByDocumentId("87654321")).thenReturn(Mono.just(true));
            
            StepVerifier.create(patientService.updatePatient(patientId, updateData))
                    .expectError(DuplicateResourceException.class)
                    .verify();
        }
    }
    
    @Nested
    @DisplayName("Eliminación de Pacientes")
    class DeletePatientTests {
        
        @Test
        @DisplayName("Debe eliminar un paciente exitosamente")
        void shouldDeletePatientSuccessfully() {
            UUID patientId = UUID.randomUUID();
            Patient patient = Patient.builder().id(patientId).fullName("Paciente").build();
            
            when(patientRepository.findById(patientId)).thenReturn(Mono.just(patient));
            when(patientRepository.deleteById(patientId)).thenReturn(Mono.empty());
            
            StepVerifier.create(patientService.deletePatient(patientId))
                    .verifyComplete();
            
            verify(patientRepository, times(1)).deleteById(patientId);
        }
    }
}
