package com.medisalud.infrastructure.persistence.adapter;

import com.medisalud.application.port.output.PatientRepository;
import com.medisalud.domain.model.Patient;
import com.medisalud.infrastructure.persistence.mapper.PatientMapper;
import com.medisalud.infrastructure.persistence.repository.R2dbcPatientRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.UUID;

@Component
@RequiredArgsConstructor
public class PatientRepositoryAdapter implements PatientRepository {
    
    private final R2dbcPatientRepository r2dbcRepository;
    private final PatientMapper mapper;
    
    @Override
    public Mono<Patient> save(Patient patient) {
        return r2dbcRepository.save(mapper.toEntity(patient))
                .map(mapper::toDomain);
    }
    
    @Override
    public Mono<Patient> findById(UUID id) {
        return r2dbcRepository.findById(id)
                .map(mapper::toDomain);
    }
    
    @Override
    public Mono<Patient> findByDocumentId(String documentId) {
        return r2dbcRepository.findByDocumentId(documentId)
                .map(mapper::toDomain);
    }
    
    @Override
    public Flux<Patient> findAll() {
        return r2dbcRepository.findAll()
                .map(mapper::toDomain);
    }
    
    @Override
    public Flux<Patient> findAllPaginated(int limit, long offset) {
        return r2dbcRepository.findAllPaginated(limit, offset)
                .map(mapper::toDomain);
    }
    
    @Override
    public Mono<Long> count() {
        return r2dbcRepository.count();
    }
    
    @Override
    public Mono<Patient> update(Patient patient) {
        return r2dbcRepository.save(mapper.toEntityForUpdate(patient))
                .map(mapper::toDomain);
    }
    
    @Override
    public Mono<Void> deleteById(UUID id) {
        return r2dbcRepository.deleteById(id);
    }
    
    @Override
    public Mono<Boolean> existsById(UUID id) {
        return r2dbcRepository.existsById(id);
    }
    
    @Override
    public Mono<Boolean> existsByDocumentId(String documentId) {
        return r2dbcRepository.existsByDocumentId(documentId);
    }
}
