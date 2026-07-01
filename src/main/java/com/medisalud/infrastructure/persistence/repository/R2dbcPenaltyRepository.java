package com.medisalud.infrastructure.persistence.repository;

import com.medisalud.infrastructure.persistence.entity.PenaltyEntity;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.time.LocalDateTime;
import java.util.UUID;

@Repository
public interface R2dbcPenaltyRepository extends ReactiveCrudRepository<PenaltyEntity, UUID> {
    Flux<PenaltyEntity> findByPatientId(UUID patientId);
    Mono<Long> countByPatientIdAndPenaltyDateTimeAfter(UUID patientId, LocalDateTime after);
    Flux<PenaltyEntity> findByPatientIdAndPenaltyDateTimeAfter(UUID patientId, LocalDateTime after);
}
