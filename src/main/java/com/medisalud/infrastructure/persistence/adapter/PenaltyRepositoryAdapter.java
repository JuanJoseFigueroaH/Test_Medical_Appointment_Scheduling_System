package com.medisalud.infrastructure.persistence.adapter;

import com.medisalud.application.port.output.PenaltyRepository;
import com.medisalud.domain.model.Penalty;
import com.medisalud.infrastructure.persistence.mapper.PenaltyMapper;
import com.medisalud.infrastructure.persistence.repository.R2dbcPenaltyRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.time.LocalDateTime;
import java.util.UUID;

@Component
@RequiredArgsConstructor
public class PenaltyRepositoryAdapter implements PenaltyRepository {
    
    private final R2dbcPenaltyRepository r2dbcRepository;
    private final PenaltyMapper mapper;
    
    @Override
    public Mono<Penalty> save(Penalty penalty) {
        return r2dbcRepository.save(mapper.toEntity(penalty))
                .map(mapper::toDomain);
    }
    
    @Override
    public Mono<Penalty> findById(UUID id) {
        return r2dbcRepository.findById(id)
                .map(mapper::toDomain);
    }
    
    @Override
    public Flux<Penalty> findByPatientId(UUID patientId) {
        return r2dbcRepository.findByPatientId(patientId)
                .map(mapper::toDomain);
    }
    
    @Override
    public Mono<Long> countByPatientIdAndPenaltyDateTimeAfter(UUID patientId, LocalDateTime after) {
        return r2dbcRepository.countByPatientIdAndPenaltyDateTimeAfter(patientId, after);
    }
    
    @Override
    public Flux<Penalty> findByPatientIdAndPenaltyDateTimeAfter(UUID patientId, LocalDateTime after) {
        return r2dbcRepository.findByPatientIdAndPenaltyDateTimeAfter(patientId, after)
                .map(mapper::toDomain);
    }
}
