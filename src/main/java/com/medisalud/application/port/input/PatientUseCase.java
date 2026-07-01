package com.medisalud.application.port.input;

import com.medisalud.domain.model.Patient;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.UUID;

public interface PatientUseCase {
    Mono<Patient> createPatient(Patient patient);
    Mono<Patient> getPatientById(UUID id);
    Mono<Patient> getPatientByDocumentId(String documentId);
    Flux<Patient> getAllPatients();
    Flux<Patient> getAllPatientsPaginated(int page, int size);
    Mono<Long> countPatients();
    Mono<Patient> updatePatient(UUID id, Patient patient);
    Mono<Void> deletePatient(UUID id);
}
