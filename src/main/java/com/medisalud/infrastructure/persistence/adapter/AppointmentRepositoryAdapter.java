package com.medisalud.infrastructure.persistence.adapter;

import com.medisalud.application.port.output.AppointmentRepository;
import com.medisalud.domain.model.Appointment;
import com.medisalud.domain.model.AppointmentStatus;
import com.medisalud.infrastructure.persistence.mapper.AppointmentMapper;
import com.medisalud.infrastructure.persistence.repository.R2dbcAppointmentRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import com.medisalud.infrastructure.exception.InfrastructureException;
import io.r2dbc.spi.R2dbcException;
import org.springframework.dao.OptimisticLockingFailureException;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.util.retry.Retry;

import java.io.IOException;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.UUID;

@Component
@RequiredArgsConstructor
public class AppointmentRepositoryAdapter implements AppointmentRepository {
    
    private static final Duration TIMEOUT = Duration.ofSeconds(5);
    private static final Retry RETRY_SPEC = Retry.backoff(3, Duration.ofMillis(100))
            .maxBackoff(Duration.ofSeconds(1))
            .filter(AppointmentRepositoryAdapter::isRetriableException);
    
    private final R2dbcAppointmentRepository r2dbcRepository;
    private final AppointmentMapper mapper;
    
    @Override
    public Mono<Appointment> save(Appointment appointment) {
        return r2dbcRepository.save(mapper.toEntity(appointment))
                .map(mapper::toDomain)
                .timeout(TIMEOUT)
                .retryWhen(RETRY_SPEC)
                .onErrorMap(R2dbcException.class, e -> 
                        new InfrastructureException("Error al guardar cita en base de datos", e));
    }
    
    @Override
    public Mono<Appointment> findById(UUID id) {
        return r2dbcRepository.findById(id)
                .map(mapper::toDomain)
                .timeout(TIMEOUT)
                .retryWhen(RETRY_SPEC);
    }
    
    @Override
    public Flux<Appointment> findAll() {
        return r2dbcRepository.findAll()
                .map(mapper::toDomain)
                .timeout(TIMEOUT)
                .retryWhen(RETRY_SPEC);
    }
    
    @Override
    public Mono<Appointment> update(Appointment appointment) {
        return r2dbcRepository.save(mapper.toEntityForUpdate(appointment))
                .map(mapper::toDomain)
                .timeout(TIMEOUT)
                .retryWhen(RETRY_SPEC)
                .onErrorMap(OptimisticLockingFailureException.class, e -> 
                        new com.medisalud.domain.exception.BusinessRuleException("CONCURRENT_MODIFICATION",
                                "La cita fue modificada por otro proceso. Por favor, intente de nuevo."))
                .onErrorMap(R2dbcException.class, e -> 
                        new InfrastructureException("Error al actualizar cita en base de datos", e));
    }
    
    @Override
    public Mono<Void> deleteById(UUID id) {
        return r2dbcRepository.deleteById(id)
                .timeout(TIMEOUT);
    }
    
    @Override
    public Flux<Appointment> findByDoctorIdAndDateTimeBetween(UUID doctorId, LocalDateTime start, LocalDateTime end) {
        return r2dbcRepository.findByDoctorIdAndDateTimeBetween(doctorId, start, end)
                .map(mapper::toDomain)
                .timeout(TIMEOUT)
                .retryWhen(RETRY_SPEC);
    }
    
    @Override
    public Flux<Appointment> findByPatientIdAndDoctorIdAndDateTimeAndStatus(UUID patientId, UUID doctorId,
                                                                             LocalDateTime dateTime, AppointmentStatus status) {
        return r2dbcRepository.findByPatientIdAndDoctorIdAndDateTimeAndStatus(patientId, doctorId, dateTime, status.name())
                .map(mapper::toDomain)
                .timeout(TIMEOUT)
                .retryWhen(RETRY_SPEC);
    }
    
    @Override
    public Mono<Boolean> existsByDoctorIdAndDateTimeAndStatus(UUID doctorId, LocalDateTime dateTime, AppointmentStatus status) {
        return r2dbcRepository.existsByDoctorIdAndDateTimeAndStatus(doctorId, dateTime, status.name())
                .timeout(TIMEOUT)
                .retryWhen(RETRY_SPEC);
    }
    
    @Override
    public Mono<Boolean> existsByPatientIdAndDoctorIdAndDateTimeAndStatus(UUID patientId, UUID doctorId,
                                                                           LocalDateTime dateTime, AppointmentStatus status) {
        return r2dbcRepository.existsByPatientIdAndDoctorIdAndDateTimeAndStatus(patientId, doctorId, dateTime, status.name())
                .timeout(TIMEOUT)
                .retryWhen(RETRY_SPEC);
    }
    
    @Override
    public Mono<Boolean> existsByPatientIdAndDateTimeAndStatus(UUID patientId, LocalDateTime dateTime, AppointmentStatus status) {
        return r2dbcRepository.existsByPatientIdAndDateTimeAndStatus(patientId, dateTime, status.name())
                .timeout(TIMEOUT)
                .retryWhen(RETRY_SPEC);
    }
    
    @Override
    public Flux<Appointment> findWithFilters(UUID doctorId, UUID patientId, AppointmentStatus status,
                                              LocalDateTime startDate, LocalDateTime endDate) {
        String statusStr = status != null ? status.name() : null;
        return r2dbcRepository.findWithFilters(doctorId, patientId, statusStr, startDate, endDate)
                .map(mapper::toDomain)
                .timeout(TIMEOUT)
                .retryWhen(RETRY_SPEC);
    }
    
    @Override
    public Flux<Appointment> findWithFiltersPaginated(UUID doctorId, UUID patientId, AppointmentStatus status,
                                                       LocalDateTime startDate, LocalDateTime endDate,
                                                       int limit, long offset) {
        String statusStr = status != null ? status.name() : null;
        return r2dbcRepository.findWithFiltersPaginated(doctorId, patientId, statusStr, startDate, endDate, limit, offset)
                .map(mapper::toDomain)
                .timeout(TIMEOUT)
                .retryWhen(RETRY_SPEC);
    }
    
    @Override
    public Mono<Long> countWithFilters(UUID doctorId, UUID patientId, AppointmentStatus status,
                                        LocalDateTime startDate, LocalDateTime endDate) {
        String statusStr = status != null ? status.name() : null;
        return r2dbcRepository.countWithFilters(doctorId, patientId, statusStr, startDate, endDate)
                .timeout(TIMEOUT)
                .retryWhen(RETRY_SPEC);
    }
    
    private static boolean isRetriableException(Throwable throwable) {
        return throwable instanceof R2dbcException 
                || throwable instanceof IOException
                || throwable instanceof java.net.ConnectException
                || throwable instanceof java.util.concurrent.TimeoutException;
    }
}
