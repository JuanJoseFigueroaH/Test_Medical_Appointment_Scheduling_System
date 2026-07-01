package com.medisalud.application.port.output;

import com.medisalud.domain.model.Penalty;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.time.LocalDateTime;
import java.util.UUID;

public interface PenaltyRepository {
    Mono<Penalty> save(Penalty penalty);
    Mono<Penalty> findById(UUID id);
    Flux<Penalty> findByPatientId(UUID patientId);
    Mono<Long> countByPatientIdAndPenaltyDateTimeAfter(UUID patientId, LocalDateTime after);
    Flux<Penalty> findByPatientIdAndPenaltyDateTimeAfter(UUID patientId, LocalDateTime after);
}
