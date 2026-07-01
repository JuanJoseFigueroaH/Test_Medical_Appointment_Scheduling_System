package com.medisalud.application.service;

import com.medisalud.application.port.input.PatientUseCase;
import com.medisalud.application.port.output.AppointmentRepository;
import com.medisalud.application.port.output.PatientRepository;
import com.medisalud.domain.exception.BusinessRuleException;
import com.medisalud.domain.exception.DuplicateResourceException;
import com.medisalud.domain.exception.ResourceNotFoundException;
import com.medisalud.domain.model.AppointmentStatus;
import com.medisalud.domain.model.Patient;
import lombok.RequiredArgsConstructor;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class PatientService implements PatientUseCase {
    
    private final PatientRepository patientRepository;
    private final AppointmentRepository appointmentRepository;
    
    @Override
    @CacheEvict(value = "patients", allEntries = true)
    public Mono<Patient> createPatient(Patient patient) {
        String documentIdValue = patient.getDocumentIdValue();
        return patientRepository.existsByDocumentId(documentIdValue)
                .flatMap(exists -> {
                    if (exists) {
                        return Mono.error(new DuplicateResourceException(
                                "Ya existe un paciente con el documento de identidad: " + documentIdValue));
                    }
                    Patient patientWithId = patient.toBuilder().id(UUID.randomUUID()).build();
                    return patientRepository.save(patientWithId);
                });
    }
    
    @Override
    @Cacheable(value = "patients", key = "#id")
    public Mono<Patient> getPatientById(UUID id) {
        return patientRepository.findById(id)
                .switchIfEmpty(Mono.error(new ResourceNotFoundException("Paciente", id.toString())));
    }
    
    @Override
    @Cacheable(value = "patients", key = "#documentId")
    public Mono<Patient> getPatientByDocumentId(String documentId) {
        return patientRepository.findByDocumentId(documentId)
                .switchIfEmpty(Mono.error(new ResourceNotFoundException("Paciente", documentId)));
    }
    
    @Override
    @Cacheable(value = "patients", key = "'all'")
    public Flux<Patient> getAllPatients() {
        return patientRepository.findAll();
    }
    
    @Override
    public Flux<Patient> getAllPatientsPaginated(int page, int size) {
        long offset = (long) page * size;
        return patientRepository.findAllPaginated(size, offset);
    }
    
    @Override
    public Mono<Long> countPatients() {
        return patientRepository.count();
    }
    
    @Override
    @CacheEvict(value = "patients", allEntries = true)
    public Mono<Patient> updatePatient(UUID id, Patient patient) {
        return patientRepository.findById(id)
                .switchIfEmpty(Mono.error(new ResourceNotFoundException("Paciente", id.toString())))
                .flatMap(existingPatient -> {
                    String existingDocId = existingPatient.getDocumentIdValue();
                    String newDocId = patient.getDocumentIdValue();
                    if (!existingDocId.equals(newDocId)) {
                        return patientRepository.existsByDocumentId(newDocId)
                                .flatMap(exists -> {
                                    if (exists) {
                                        return Mono.error(new DuplicateResourceException(
                                                "Ya existe un paciente con el documento de identidad: " + newDocId));
                                    }
                                    return updatePatientFields(existingPatient, patient);
                                });
                    }
                    return updatePatientFields(existingPatient, patient);
                });
    }
    
    private Mono<Patient> updatePatientFields(Patient existingPatient, Patient patient) {
        Patient updated = existingPatient.toBuilder()
                .fullName(patient.getFullName())
                .documentId(patient.getDocumentId())
                .phone(patient.getPhone())
                .email(patient.getEmail())
                .birthDate(patient.getBirthDate())
                .build();
        return patientRepository.update(updated);
    }
    
    @Override
    @CacheEvict(value = "patients", allEntries = true)
    public Mono<Void> deletePatient(UUID id) {
        return patientRepository.findById(id)
                .switchIfEmpty(Mono.error(new ResourceNotFoundException("Paciente", id.toString())))
                .flatMap(patient -> 
                    appointmentRepository.findWithFilters(null, id, AppointmentStatus.PROGRAMADA, null, null)
                            .hasElements()
                            .flatMap(hasAppointments -> {
                                if (hasAppointments) {
                                    return Mono.error(new BusinessRuleException("PATIENT_HAS_APPOINTMENTS",
                                            "No se puede eliminar el paciente porque tiene citas programadas"));
                                }
                                return patientRepository.deleteById(id);
                            })
                );
    }
}
