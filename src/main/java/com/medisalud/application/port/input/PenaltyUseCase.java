package com.medisalud.application.port.input;

import com.medisalud.domain.model.Appointment;
import com.medisalud.domain.model.Penalty;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.UUID;

public interface PenaltyUseCase {
    Mono<Void> checkPatientCanSchedule(UUID patientId);
    Mono<Boolean> shouldApplyPenalty(Appointment appointment);
    Mono<Penalty> applyPenalty(Appointment appointment);
    Mono<Long> getPatientPenaltyCount(UUID patientId);
    Flux<Penalty> getPatientPenalties(UUID patientId);
}
