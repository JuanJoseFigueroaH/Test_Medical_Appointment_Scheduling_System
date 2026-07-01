package com.medisalud.application.port.output;

import com.medisalud.domain.model.Patient;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.UUID;

public interface PatientRepository {
    Mono<Patient> save(Patient patient);
    Mono<Patient> findById(UUID id);
    Mono<Patient> findByDocumentId(String documentId);
    Flux<Patient> findAll();
    Flux<Patient> findAllPaginated(int limit, long offset);
    Mono<Long> count();
    Mono<Patient> update(Patient patient);
    Mono<Void> deleteById(UUID id);
    Mono<Boolean> existsById(UUID id);
    Mono<Boolean> existsByDocumentId(String documentId);
}
