package com.medisalud.infrastructure.persistence.adapter;

import com.medisalud.application.port.output.DoctorRepository;
import com.medisalud.domain.model.Doctor;
import com.medisalud.infrastructure.persistence.mapper.DoctorMapper;
import com.medisalud.infrastructure.persistence.repository.R2dbcDoctorRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.UUID;

@Component
@RequiredArgsConstructor
public class DoctorRepositoryAdapter implements DoctorRepository {
    
    private final R2dbcDoctorRepository r2dbcRepository;
    private final DoctorMapper mapper;
    
    @Override
    public Mono<Doctor> save(Doctor doctor) {
        return r2dbcRepository.save(mapper.toEntity(doctor))
                .map(mapper::toDomain);
    }
    
    @Override
    public Mono<Doctor> findById(UUID id) {
        return r2dbcRepository.findById(id)
                .map(mapper::toDomain);
    }
    
    @Override
    public Flux<Doctor> findAll() {
        return r2dbcRepository.findAll()
                .map(mapper::toDomain);
    }
    
    @Override
    public Flux<Doctor> findAllPaginated(int limit, long offset) {
        return r2dbcRepository.findAllPaginated(limit, offset)
                .map(mapper::toDomain);
    }
    
    @Override
    public Mono<Long> count() {
        return r2dbcRepository.count();
    }
    
    @Override
    public Mono<Doctor> update(Doctor doctor) {
        return r2dbcRepository.save(mapper.toEntityForUpdate(doctor))
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
}
