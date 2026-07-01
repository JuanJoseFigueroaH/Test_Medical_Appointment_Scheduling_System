package com.medisalud.application.validation;

import com.medisalud.application.port.input.PenaltyUseCase;
import com.medisalud.application.port.output.AppointmentRepository;
import com.medisalud.application.port.output.DoctorRepository;
import com.medisalud.application.port.output.PatientRepository;
import com.medisalud.domain.exception.AppointmentConflictException;
import com.medisalud.domain.exception.BusinessRuleException;
import com.medisalud.domain.exception.ResourceNotFoundException;
import com.medisalud.domain.model.AppointmentStatus;
import com.medisalud.application.service.TimeSlotApplicationService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;
import java.util.function.Supplier;

@Component
@RequiredArgsConstructor
public class AppointmentValidator {
    
    private static final int MAX_DATE_RANGE_DAYS = 90;
    
    private final AppointmentRepository appointmentRepository;
    private final DoctorRepository doctorRepository;
    private final PatientRepository patientRepository;
    private final TimeSlotApplicationService timeSlotService;
    private final PenaltyUseCase penaltyService;
    
    private <T extends Throwable> Mono<Void> validateExists(Mono<Boolean> existsCheck, Supplier<T> errorSupplier) {
        return existsCheck.flatMap(exists -> exists ? Mono.empty() : Mono.error(errorSupplier.get()));
    }
    
    private <T extends Throwable> Mono<Void> validateNotExists(Mono<Boolean> existsCheck, Supplier<T> errorSupplier) {
        return existsCheck.flatMap(exists -> exists ? Mono.error(errorSupplier.get()) : Mono.empty());
    }
    
    public Mono<Void> validateForCreation(UUID patientId, UUID doctorId, LocalDateTime dateTime) {
        return validateEntitiesExist(patientId, doctorId)
                .then(timeSlotService.validateTimeSlot(dateTime))
                .then(penaltyService.checkPatientCanSchedule(patientId))
                .then(checkDoctorAvailability(doctorId, dateTime))
                .then(checkPatientConflict(patientId, doctorId, dateTime));
    }
    
    public Mono<Void> validateForReschedule(UUID patientId, UUID doctorId, LocalDateTime newDateTime) {
        return timeSlotService.validateTimeSlot(newDateTime)
                .then(checkDoctorAvailability(doctorId, newDateTime))
                .then(checkPatientConflict(patientId, doctorId, newDateTime));
    }
    
    public Mono<Void> validateDateRange(LocalDate startDate, LocalDate endDate) {
        return Mono.defer(() -> {
            if (startDate == null || endDate == null) {
                return Mono.error(new BusinessRuleException("INVALID_DATE_RANGE", 
                        "Las fechas de inicio y fin son obligatorias"));
            }
            
            if (startDate.isAfter(endDate)) {
                return Mono.error(new BusinessRuleException("INVALID_DATE_RANGE", 
                        "La fecha de inicio no puede ser posterior a la fecha de fin"));
            }
            
            long daysBetween = java.time.temporal.ChronoUnit.DAYS.between(startDate, endDate);
            if (daysBetween > MAX_DATE_RANGE_DAYS) {
                return Mono.error(new BusinessRuleException("DATE_RANGE_TOO_LARGE", 
                        String.format("El rango de fechas no puede exceder %d días", MAX_DATE_RANGE_DAYS)));
            }
            
            return Mono.empty();
        });
    }
    
    public Mono<Void> validateDoctorExists(UUID doctorId) {
        return validateExists(
                doctorRepository.existsById(doctorId),
                () -> new ResourceNotFoundException("Médico", doctorId.toString()));
    }
    
    private Mono<Void> validateEntitiesExist(UUID patientId, UUID doctorId) {
        Mono<Void> patientExists = validateExists(
                patientRepository.existsById(patientId),
                () -> new ResourceNotFoundException("Paciente", patientId.toString()));
        
        Mono<Void> doctorExists = validateExists(
                doctorRepository.existsById(doctorId),
                () -> new ResourceNotFoundException("Médico", doctorId.toString()));
        
        return Mono.when(patientExists, doctorExists);
    }
    
    private Mono<Void> checkDoctorAvailability(UUID doctorId, LocalDateTime dateTime) {
        return validateNotExists(
                appointmentRepository.existsByDoctorIdAndDateTimeAndStatus(doctorId, dateTime, AppointmentStatus.PROGRAMADA),
                () -> new AppointmentConflictException("El médico ya tiene una cita programada en este horario: " + dateTime));
    }
    
    private Mono<Void> checkPatientConflict(UUID patientId, UUID doctorId, LocalDateTime dateTime) {
        return validateNotExists(
                appointmentRepository.existsByPatientIdAndDoctorIdAndDateTimeAndStatus(patientId, doctorId, dateTime, AppointmentStatus.PROGRAMADA),
                () -> new AppointmentConflictException("El paciente ya tiene una cita programada con este médico en este horario: " + dateTime));
    }
}
