package com.medisalud.application.port.output;

import com.medisalud.domain.model.Appointment;
import com.medisalud.domain.model.AppointmentStatus;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.time.LocalDateTime;
import java.util.UUID;

public interface AppointmentRepository {
    Mono<Appointment> save(Appointment appointment);
    Mono<Appointment> findById(UUID id);
    Flux<Appointment> findAll();
    Mono<Appointment> update(Appointment appointment);
    Mono<Void> deleteById(UUID id);
    
    Flux<Appointment> findByDoctorIdAndDateTimeBetween(UUID doctorId, LocalDateTime start, LocalDateTime end);
    
    Flux<Appointment> findByPatientIdAndDoctorIdAndDateTimeAndStatus(UUID patientId, UUID doctorId, 
                                                                      LocalDateTime dateTime, AppointmentStatus status);
    
    Mono<Boolean> existsByDoctorIdAndDateTimeAndStatus(UUID doctorId, LocalDateTime dateTime, AppointmentStatus status);
    
    Mono<Boolean> existsByPatientIdAndDoctorIdAndDateTimeAndStatus(UUID patientId, UUID doctorId, 
                                                                    LocalDateTime dateTime, AppointmentStatus status);
    
    Mono<Boolean> existsByPatientIdAndDateTimeAndStatus(UUID patientId, LocalDateTime dateTime, AppointmentStatus status);
    
    Flux<Appointment> findWithFilters(UUID doctorId, UUID patientId, AppointmentStatus status,
                                       LocalDateTime startDate, LocalDateTime endDate);
    
    Flux<Appointment> findWithFiltersPaginated(UUID doctorId, UUID patientId, AppointmentStatus status,
                                                LocalDateTime startDate, LocalDateTime endDate,
                                                int limit, long offset);
    
    Mono<Long> countWithFilters(UUID doctorId, UUID patientId, AppointmentStatus status,
                                 LocalDateTime startDate, LocalDateTime endDate);
}
