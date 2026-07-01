package com.medisalud.application.service;

import com.medisalud.application.port.input.DoctorUseCase;
import com.medisalud.application.port.output.AppointmentRepository;
import com.medisalud.application.port.output.DoctorRepository;
import com.medisalud.domain.exception.BusinessRuleException;
import com.medisalud.domain.exception.ResourceNotFoundException;
import com.medisalud.domain.model.AppointmentStatus;
import com.medisalud.domain.model.Doctor;
import lombok.RequiredArgsConstructor;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class DoctorService implements DoctorUseCase {
    
    private final DoctorRepository doctorRepository;
    private final AppointmentRepository appointmentRepository;
    
    @Override
    @CacheEvict(value = "doctors", allEntries = true)
    public Mono<Doctor> createDoctor(Doctor doctor) {
        Doctor doctorWithId = doctor.toBuilder().id(UUID.randomUUID()).build();
        return doctorRepository.save(doctorWithId);
    }
    
    @Override
    @Cacheable(value = "doctors", key = "#id")
    public Mono<Doctor> getDoctorById(UUID id) {
        return doctorRepository.findById(id)
                .switchIfEmpty(Mono.error(new ResourceNotFoundException("Médico", id.toString())));
    }
    
    @Override
    @Cacheable(value = "doctors", key = "'all'")
    public Flux<Doctor> getAllDoctors() {
        return doctorRepository.findAll();
    }
    
    @Override
    public Flux<Doctor> getAllDoctorsPaginated(int page, int size) {
        long offset = (long) page * size;
        return doctorRepository.findAllPaginated(size, offset);
    }
    
    @Override
    public Mono<Long> countDoctors() {
        return doctorRepository.count();
    }
    
    @Override
    @CacheEvict(value = "doctors", allEntries = true)
    public Mono<Doctor> updateDoctor(UUID id, Doctor doctor) {
        return doctorRepository.findById(id)
                .switchIfEmpty(Mono.error(new ResourceNotFoundException("Médico", id.toString())))
                .flatMap(existingDoctor -> {
                    Doctor updated = existingDoctor.toBuilder()
                            .fullName(doctor.getFullName())
                            .specialty(doctor.getSpecialty())
                            .phone(doctor.getPhone())
                            .email(doctor.getEmail())
                            .build();
                    return doctorRepository.update(updated);
                });
    }
    
    @Override
    @CacheEvict(value = "doctors", allEntries = true)
    public Mono<Void> deleteDoctor(UUID id) {
        return doctorRepository.findById(id)
                .switchIfEmpty(Mono.error(new ResourceNotFoundException("Médico", id.toString())))
                .flatMap(doctor -> 
                    appointmentRepository.findWithFilters(id, null, AppointmentStatus.PROGRAMADA, null, null)
                            .hasElements()
                            .flatMap(hasAppointments -> {
                                if (hasAppointments) {
                                    return Mono.error(new BusinessRuleException("DOCTOR_HAS_APPOINTMENTS",
                                            "No se puede eliminar el médico porque tiene citas programadas"));
                                }
                                return doctorRepository.deleteById(id);
                            })
                );
    }
}
