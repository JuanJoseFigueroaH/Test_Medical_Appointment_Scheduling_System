package com.medisalud.infrastructure.persistence.repository;

import com.medisalud.infrastructure.persistence.entity.AppointmentEntity;
import org.springframework.data.r2dbc.repository.Query;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.time.LocalDateTime;
import java.util.UUID;

@Repository
public interface R2dbcAppointmentRepository extends ReactiveCrudRepository<AppointmentEntity, UUID> {
    
    Flux<AppointmentEntity> findByDoctorIdAndDateTimeBetween(UUID doctorId, LocalDateTime start, LocalDateTime end);
    
    Mono<Boolean> existsByDoctorIdAndDateTimeAndStatus(UUID doctorId, LocalDateTime dateTime, String status);
    
    Mono<Boolean> existsByPatientIdAndDoctorIdAndDateTimeAndStatus(UUID patientId, UUID doctorId, 
                                                                    LocalDateTime dateTime, String status);
    
    Mono<Boolean> existsByPatientIdAndDateTimeAndStatus(UUID patientId, LocalDateTime dateTime, String status);
    
    Flux<AppointmentEntity> findByPatientIdAndDoctorIdAndDateTimeAndStatus(UUID patientId, UUID doctorId,
                                                                            LocalDateTime dateTime, String status);
    
    @Query("SELECT * FROM appointments WHERE " +
           "(CAST(:doctorId AS UUID) IS NULL OR doctor_id = :doctorId) AND " +
           "(CAST(:patientId AS UUID) IS NULL OR patient_id = :patientId) AND " +
           "(CAST(:status AS VARCHAR) IS NULL OR status = :status) AND " +
           "(CAST(:startDate AS TIMESTAMP) IS NULL OR date_time >= :startDate) AND " +
           "(CAST(:endDate AS TIMESTAMP) IS NULL OR date_time <= :endDate) " +
           "ORDER BY date_time DESC")
    Flux<AppointmentEntity> findWithFilters(UUID doctorId, UUID patientId, String status,
                                             LocalDateTime startDate, LocalDateTime endDate);
    
    @Query("SELECT * FROM appointments WHERE " +
           "(CAST(:doctorId AS UUID) IS NULL OR doctor_id = :doctorId) AND " +
           "(CAST(:patientId AS UUID) IS NULL OR patient_id = :patientId) AND " +
           "(CAST(:status AS VARCHAR) IS NULL OR status = :status) AND " +
           "(CAST(:startDate AS TIMESTAMP) IS NULL OR date_time >= :startDate) AND " +
           "(CAST(:endDate AS TIMESTAMP) IS NULL OR date_time <= :endDate) " +
           "ORDER BY date_time DESC " +
           "LIMIT :limit OFFSET :offset")
    Flux<AppointmentEntity> findWithFiltersPaginated(UUID doctorId, UUID patientId, String status,
                                                      LocalDateTime startDate, LocalDateTime endDate,
                                                      int limit, long offset);
    
    @Query("SELECT COUNT(*) FROM appointments WHERE " +
           "(CAST(:doctorId AS UUID) IS NULL OR doctor_id = :doctorId) AND " +
           "(CAST(:patientId AS UUID) IS NULL OR patient_id = :patientId) AND " +
           "(CAST(:status AS VARCHAR) IS NULL OR status = :status) AND " +
           "(CAST(:startDate AS TIMESTAMP) IS NULL OR date_time >= :startDate) AND " +
           "(CAST(:endDate AS TIMESTAMP) IS NULL OR date_time <= :endDate)")
    Mono<Long> countWithFilters(UUID doctorId, UUID patientId, String status,
                                 LocalDateTime startDate, LocalDateTime endDate);
}
