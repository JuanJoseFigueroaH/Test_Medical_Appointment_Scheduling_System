package com.medisalud.application.port.input;

import com.medisalud.domain.model.Appointment;
import com.medisalud.domain.model.AppointmentStatus;
import com.medisalud.domain.model.TimeSlot;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

public interface AppointmentUseCase {
    Mono<Appointment> createAppointment(UUID patientId, UUID doctorId, LocalDateTime dateTime);
    
    Mono<Appointment> getAppointmentById(UUID id);
    
    Flux<TimeSlot> getAvailableSlots(UUID doctorId, LocalDate startDate, LocalDate endDate);
    
    Mono<Appointment> cancelAppointment(UUID appointmentId);
    
    Mono<Appointment> rescheduleAppointment(UUID appointmentId, LocalDateTime newDateTime);
    
    Flux<Appointment> listAppointments(UUID doctorId, UUID patientId, AppointmentStatus status, 
                                        LocalDate startDate, LocalDate endDate);
    
    Flux<Appointment> listAppointmentsPaginated(UUID doctorId, UUID patientId, AppointmentStatus status,
                                                 LocalDate startDate, LocalDate endDate, int limit, long offset);
    
    Mono<Long> countAppointments(UUID doctorId, UUID patientId, AppointmentStatus status,
                                  LocalDate startDate, LocalDate endDate);
    
    Mono<Appointment> markAsAttended(UUID appointmentId);
}
