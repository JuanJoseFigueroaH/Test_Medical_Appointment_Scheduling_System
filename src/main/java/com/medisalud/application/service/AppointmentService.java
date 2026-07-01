package com.medisalud.application.service;

import com.medisalud.application.port.input.AppointmentUseCase;
import com.medisalud.application.port.input.PenaltyUseCase;
import com.medisalud.application.port.output.AppointmentRepository;
import com.medisalud.application.validation.AppointmentValidator;
import com.medisalud.domain.exception.BusinessRuleException;
import com.medisalud.domain.exception.ResourceNotFoundException;
import com.medisalud.domain.model.Appointment;
import com.medisalud.domain.model.AppointmentStatus;
import com.medisalud.domain.model.TimeSlot;
import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.MeterRegistry;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.reactive.TransactionalOperator;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.time.Clock;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Slf4j
@Service
public class AppointmentService implements AppointmentUseCase {
    
    private final AppointmentRepository appointmentRepository;
    private final TimeSlotApplicationService timeSlotService;
    private final PenaltyUseCase penaltyService;
    private final AppointmentValidator appointmentValidator;
    private final TransactionalOperator transactionalOperator;
    private final Counter appointmentsCreatedCounter;
    private final Counter appointmentsCancelledCounter;
    private final Counter appointmentsRescheduledCounter;
    private final Clock clock;
    
    @Autowired
    public AppointmentService(AppointmentRepository appointmentRepository,
                              TimeSlotApplicationService timeSlotService,
                              PenaltyUseCase penaltyService,
                              AppointmentValidator appointmentValidator,
                              TransactionalOperator transactionalOperator,
                              MeterRegistry meterRegistry,
                              Clock clock) {
        this.appointmentRepository = appointmentRepository;
        this.timeSlotService = timeSlotService;
        this.penaltyService = penaltyService;
        this.appointmentValidator = appointmentValidator;
        this.transactionalOperator = transactionalOperator;
        this.appointmentsCreatedCounter = meterRegistry.counter("medisalud.appointments.created", "type", "created");
        this.appointmentsCancelledCounter = meterRegistry.counter("medisalud.appointments.cancelled", "type", "cancelled");
        this.appointmentsRescheduledCounter = meterRegistry.counter("medisalud.appointments.rescheduled", "type", "rescheduled");
        this.clock = clock;
    }
    
    AppointmentService(AppointmentRepository appointmentRepository,
                       TimeSlotApplicationService timeSlotService,
                       PenaltyUseCase penaltyService,
                       AppointmentValidator appointmentValidator,
                       TransactionalOperator transactionalOperator,
                       Counter appointmentsCreatedCounter,
                       Counter appointmentsCancelledCounter,
                       Counter appointmentsRescheduledCounter,
                       Clock clock) {
        this.appointmentRepository = appointmentRepository;
        this.timeSlotService = timeSlotService;
        this.penaltyService = penaltyService;
        this.appointmentValidator = appointmentValidator;
        this.transactionalOperator = transactionalOperator;
        this.appointmentsCreatedCounter = appointmentsCreatedCounter;
        this.appointmentsCancelledCounter = appointmentsCancelledCounter;
        this.appointmentsRescheduledCounter = appointmentsRescheduledCounter;
        this.clock = clock;
    }
    
    @Override
    public Mono<Appointment> createAppointment(UUID patientId, UUID doctorId, LocalDateTime dateTime) {
        LocalDateTime normalizedDateTime = timeSlotService.normalizeToSlotStart(dateTime);
        log.info("Creating appointment for patient {} with doctor {} at {}", patientId, doctorId, normalizedDateTime);
        
        return appointmentValidator.validateForCreation(patientId, doctorId, normalizedDateTime)
                .then(Mono.defer(() -> buildAndSaveAppointment(patientId, doctorId, normalizedDateTime)))
                .doOnSuccess(apt -> {
                    appointmentsCreatedCounter.increment();
                    log.info("Appointment {} created successfully", apt.getId());
                })
                .doOnError(e -> log.error("Failed to create appointment: {}", e.getMessage()));
    }
    
    private Mono<Appointment> buildAndSaveAppointment(UUID patientId, UUID doctorId, LocalDateTime dateTime) {
        LocalDateTime now = LocalDateTime.now(clock);
        Appointment appointment = Appointment.builder()
                .id(UUID.randomUUID())
                .patientId(patientId)
                .doctorId(doctorId)
                .dateTime(dateTime)
                .status(AppointmentStatus.PROGRAMADA)
                .createdAt(now)
                .updatedAt(now)
                .build();
        return appointmentRepository.save(appointment);
    }
    
    @Override
    public Mono<Appointment> getAppointmentById(UUID id) {
        return appointmentRepository.findById(id)
                .switchIfEmpty(Mono.error(new ResourceNotFoundException("Cita", id.toString())));
    }
    
    @Override
    public Flux<TimeSlot> getAvailableSlots(UUID doctorId, LocalDate startDate, LocalDate endDate) {
        return appointmentValidator.validateDateRange(startDate, endDate)
                .then(appointmentValidator.validateDoctorExists(doctorId))
                .thenMany(Flux.defer(() -> {
                    List<TimeSlot> allSlots = timeSlotService.generateSlotsForDateRange(startDate, endDate);
                    
                    LocalDateTime start = LocalDateTime.of(startDate, LocalTime.MIN);
                    LocalDateTime end = LocalDateTime.of(endDate, LocalTime.MAX);
                    
                    return appointmentRepository.findByDoctorIdAndDateTimeBetween(doctorId, start, end)
                            .filter(apt -> apt.getStatus() == AppointmentStatus.PROGRAMADA)
                            .map(Appointment::getDateTime)
                            .collect(Collectors.toSet())
                            .flatMapMany(occupiedSlots -> {
                                LocalDateTime now = LocalDateTime.now(clock);
                                return Flux.fromIterable(allSlots)
                                        .filter(slot -> !occupiedSlots.contains(slot.getStartTime()))
                                        .filter(slot -> slot.getStartTime().isAfter(now));
                            });
                }));
    }
    
    @Override
    public Mono<Appointment> cancelAppointment(UUID appointmentId) {
        log.info("Cancelling appointment {}", appointmentId);
        return transactionalOperator.transactional(
                appointmentRepository.findById(appointmentId)
                        .switchIfEmpty(Mono.error(new ResourceNotFoundException("Cita", appointmentId.toString())))
                        .flatMap(appointment -> {
                            if (appointment.isCancelled()) {
                                log.info("Appointment {} already cancelled, returning idempotent response", appointmentId);
                                return Mono.just(appointment);
                            }
                            
                            if (!appointment.isScheduled()) {
                                return Mono.error(new BusinessRuleException("INVALID_STATUS",
                                        "Solo se pueden cancelar citas con estado PROGRAMADA"));
                            }
                            
                            return penaltyService.applyPenalty(appointment)
                                    .then(Mono.defer(() -> {
                                        Appointment cancelled = appointment.cancel(clock);
                                        return appointmentRepository.update(cancelled);
                                    }));
                        })
        )
        .doOnSuccess(apt -> {
            if (apt.isCancelled()) {
                appointmentsCancelledCounter.increment();
                log.info("Appointment {} cancelled successfully", appointmentId);
            }
        })
        .doOnError(e -> log.error("Failed to cancel appointment {}: {}", appointmentId, e.getMessage()));
    }
    
    @Override
    public Mono<Appointment> rescheduleAppointment(UUID appointmentId, LocalDateTime newDateTime) {
        LocalDateTime normalizedNewDateTime = timeSlotService.normalizeToSlotStart(newDateTime);
        log.info("Rescheduling appointment {} to {}", appointmentId, normalizedNewDateTime);
        
        return transactionalOperator.transactional(
                appointmentRepository.findById(appointmentId)
                        .switchIfEmpty(Mono.error(new ResourceNotFoundException("Cita", appointmentId.toString())))
                        .flatMap(appointment -> {
                            if (!appointment.isScheduled()) {
                                return Mono.error(new BusinessRuleException("INVALID_STATUS",
                                        "Solo se pueden reprogramar citas con estado PROGRAMADA"));
                            }
                            
                            if (appointment.isPast(clock)) {
                                return Mono.error(new BusinessRuleException("PAST_APPOINTMENT",
                                        "No se puede reprogramar una cita que ya pasó"));
                            }
                            
                            UUID patientId = appointment.getPatientId();
                            UUID doctorId = appointment.getDoctorId();
                            
                            return appointmentValidator.validateForReschedule(patientId, doctorId, normalizedNewDateTime)
                                    .then(penaltyService.applyPenalty(appointment))
                                    .then(Mono.defer(() -> {
                                        Appointment cancelled = appointment.cancel(clock);
                                        return appointmentRepository.update(cancelled);
                                    }))
                                    .then(Mono.defer(() -> buildAndSaveAppointment(patientId, doctorId, normalizedNewDateTime)));
                        })
        )
        .doOnSuccess(apt -> {
            appointmentsRescheduledCounter.increment();
            log.info("Appointment {} rescheduled to {} successfully", appointmentId, normalizedNewDateTime);
        })
        .doOnError(e -> log.error("Failed to reschedule appointment {}: {}", appointmentId, e.getMessage()));
    }
    
    @Override
    public Flux<Appointment> listAppointments(UUID doctorId, UUID patientId, AppointmentStatus status,
                                               LocalDate startDate, LocalDate endDate) {
        LocalDateTime start = startDate != null ? LocalDateTime.of(startDate, LocalTime.MIN) : null;
        LocalDateTime end = endDate != null ? LocalDateTime.of(endDate, LocalTime.MAX) : null;
        
        return appointmentRepository.findWithFilters(doctorId, patientId, status, start, end);
    }
    
    @Override
    public Flux<Appointment> listAppointmentsPaginated(UUID doctorId, UUID patientId, AppointmentStatus status,
                                                        LocalDate startDate, LocalDate endDate, int limit, long offset) {
        LocalDateTime start = startDate != null ? LocalDateTime.of(startDate, LocalTime.MIN) : null;
        LocalDateTime end = endDate != null ? LocalDateTime.of(endDate, LocalTime.MAX) : null;
        
        return appointmentRepository.findWithFiltersPaginated(doctorId, patientId, status, start, end, limit, offset);
    }
    
    @Override
    public Mono<Long> countAppointments(UUID doctorId, UUID patientId, AppointmentStatus status,
                                         LocalDate startDate, LocalDate endDate) {
        LocalDateTime start = startDate != null ? LocalDateTime.of(startDate, LocalTime.MIN) : null;
        LocalDateTime end = endDate != null ? LocalDateTime.of(endDate, LocalTime.MAX) : null;
        
        return appointmentRepository.countWithFilters(doctorId, patientId, status, start, end);
    }
    
    @Override
    public Mono<Appointment> markAsAttended(UUID appointmentId) {
        return appointmentRepository.findById(appointmentId)
                .switchIfEmpty(Mono.error(new ResourceNotFoundException("Cita", appointmentId.toString())))
                .flatMap(appointment -> {
                    if (!appointment.isScheduled()) {
                        return Mono.error(new BusinessRuleException("INVALID_STATUS",
                                "Solo se pueden marcar como atendidas las citas con estado PROGRAMADA"));
                    }
                    
                    if (!appointment.canBeMarkedAsAttended(clock)) {
                        return Mono.error(new BusinessRuleException("APPOINTMENT_NOT_DUE",
                                "No se puede marcar como atendida una cita que aún no ha llegado su hora"));
                    }
                    
                    Appointment attended = appointment.markAsAttended(clock);
                    return appointmentRepository.update(attended);
                });
    }
}
