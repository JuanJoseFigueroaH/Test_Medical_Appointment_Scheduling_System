package com.medisalud.application.port.output;

import com.medisalud.domain.model.Doctor;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.UUID;

public interface DoctorRepository {
    Mono<Doctor> save(Doctor doctor);
    Mono<Doctor> findById(UUID id);
    Flux<Doctor> findAll();
    Flux<Doctor> findAllPaginated(int limit, long offset);
    Mono<Long> count();
    Mono<Doctor> update(Doctor doctor);
    Mono<Void> deleteById(UUID id);
    Mono<Boolean> existsById(UUID id);
}
