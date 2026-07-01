package com.medisalud.application.port.input;

import com.medisalud.domain.model.Doctor;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.UUID;

public interface DoctorUseCase {
    Mono<Doctor> createDoctor(Doctor doctor);
    Mono<Doctor> getDoctorById(UUID id);
    Flux<Doctor> getAllDoctors();
    Flux<Doctor> getAllDoctorsPaginated(int page, int size);
    Mono<Long> countDoctors();
    Mono<Doctor> updateDoctor(UUID id, Doctor doctor);
    Mono<Void> deleteDoctor(UUID id);
}
