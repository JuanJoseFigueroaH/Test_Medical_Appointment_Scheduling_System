package com.medisalud.infrastructure.persistence.repository;

import com.medisalud.infrastructure.persistence.entity.DoctorEntity;
import org.springframework.data.r2dbc.repository.Query;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Flux;

import java.util.UUID;

@Repository
public interface R2dbcDoctorRepository extends ReactiveCrudRepository<DoctorEntity, UUID> {
    
    @Query("SELECT * FROM doctors ORDER BY full_name LIMIT :limit OFFSET :offset")
    Flux<DoctorEntity> findAllPaginated(int limit, long offset);
}
