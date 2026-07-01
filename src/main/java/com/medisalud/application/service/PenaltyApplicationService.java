package com.medisalud.application.service;

import com.medisalud.application.port.input.PenaltyUseCase;
import com.medisalud.application.port.output.PenaltyRepository;
import com.medisalud.domain.exception.PatientPenalizedException;
import com.medisalud.domain.model.Appointment;
import com.medisalud.domain.model.Penalty;
import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.MeterRegistry;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

import com.medisalud.domain.BusinessConstants;

import java.time.Clock;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.UUID;

@Slf4j
@Service
public class PenaltyApplicationService implements PenaltyUseCase {
    
    private final PenaltyRepository penaltyRepository;
    private final Counter penaltiesAppliedCounter;
    private final Counter lateCancellationsCounter;
    private final Clock clock;
    
    @Autowired
    public PenaltyApplicationService(PenaltyRepository penaltyRepository,
                                      MeterRegistry meterRegistry,
                                      Clock clock) {
        this.penaltyRepository = penaltyRepository;
        this.penaltiesAppliedCounter = meterRegistry.counter("medisalud.penalties.applied", "type", "applied");
        this.lateCancellationsCounter = meterRegistry.counter("medisalud.penalties.late_cancellations", "type", "late");
        this.clock = clock;
    }
    
    PenaltyApplicationService(PenaltyRepository penaltyRepository,
                              Counter penaltiesAppliedCounter,
                              Counter lateCancellationsCounter,
                              Clock clock) {
        this.penaltyRepository = penaltyRepository;
        this.penaltiesAppliedCounter = penaltiesAppliedCounter;
        this.lateCancellationsCounter = lateCancellationsCounter;
        this.clock = clock;
    }
    
    @Override
    public Mono<Void> checkPatientCanSchedule(UUID patientId) {
        LocalDateTime penaltyWindowStart = LocalDateTime.now(clock).minusDays(BusinessConstants.PENALTY_WINDOW_DAYS);
        
        return penaltyRepository.countByPatientIdAndPenaltyDateTimeAfter(patientId, penaltyWindowStart)
                .doOnNext(count -> log.debug("Patient {} has {} penalties in last {} days", 
                        patientId, count, BusinessConstants.PENALTY_WINDOW_DAYS))
                .flatMap(count -> {
                    if (count >= BusinessConstants.MAX_PENALTIES_BEFORE_BLOCK) {
                        log.warn("Patient {} blocked from scheduling - {} penalties exceed limit of {}", 
                                patientId, count, BusinessConstants.MAX_PENALTIES_BEFORE_BLOCK);
                        return Mono.error(new PatientPenalizedException(count.intValue()));
                    }
                    return Mono.empty();
                });
    }
    
    @Override
    public Mono<Boolean> shouldApplyPenalty(Appointment appointment) {
        LocalDateTime now = LocalDateTime.now(clock);
        long hoursUntilAppointment = ChronoUnit.HOURS.between(now, appointment.getDateTime());
        boolean shouldApply = hoursUntilAppointment < BusinessConstants.LATE_CANCELLATION_HOURS;
        
        log.debug("Appointment {} - hours until: {}, should apply penalty: {}", 
                appointment.getId(), hoursUntilAppointment, shouldApply);
        
        return Mono.just(shouldApply);
    }
    
    @Override
    public Mono<Penalty> applyPenalty(Appointment appointment) {
        return shouldApplyPenalty(appointment)
                .flatMap(shouldApply -> {
                    if (shouldApply) {
                        Penalty penalty = Penalty.builder()
                                .id(UUID.randomUUID())
                                .patientId(appointment.getPatientId())
                                .appointmentId(appointment.getId())
                                .penaltyDateTime(LocalDateTime.now(clock))
                                .reason("Cancelación tardía (menos de 2 horas de antelación)")
                                .build();
                        
                        log.info("Applying penalty to patient {} for late cancellation of appointment {}", 
                                appointment.getPatientId(), appointment.getId());
                        
                        return penaltyRepository.save(penalty)
                                .doOnSuccess(p -> {
                                    penaltiesAppliedCounter.increment();
                                    lateCancellationsCounter.increment();
                                });
                    }
                    return Mono.empty();
                });
    }
    
    @Override
    public Mono<Long> getPatientPenaltyCount(UUID patientId) {
        LocalDateTime penaltyWindowStart = LocalDateTime.now(clock).minusDays(BusinessConstants.PENALTY_WINDOW_DAYS);
        return penaltyRepository.countByPatientIdAndPenaltyDateTimeAfter(patientId, penaltyWindowStart);
    }
    
    @Override
    public reactor.core.publisher.Flux<Penalty> getPatientPenalties(UUID patientId) {
        LocalDateTime penaltyWindowStart = LocalDateTime.now(clock).minusDays(BusinessConstants.PENALTY_WINDOW_DAYS);
        return penaltyRepository.findByPatientIdAndPenaltyDateTimeAfter(patientId, penaltyWindowStart);
    }
}
