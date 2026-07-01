package com.medisalud.infrastructure.persistence.repository;

import com.medisalud.infrastructure.persistence.entity.PatientEntity;
import org.springframework.data.r2dbc.repository.Query;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.UUID;

@Repository
public interface R2dbcPatientRepository extends ReactiveCrudRepository<PatientEntity, UUID> {
    Mono<PatientEntity> findByDocumentId(String documentId);
    Mono<Boolean> existsByDocumentId(String documentId);
    
    @Query("SELECT * FROM patients ORDER BY full_name LIMIT :limit OFFSET :offset")
    Flux<PatientEntity> findAllPaginated(int limit, long offset);
}
