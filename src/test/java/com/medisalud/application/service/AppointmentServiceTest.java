package com.medisalud.application.service;

import com.medisalud.application.port.input.PenaltyUseCase;
import com.medisalud.application.port.output.AppointmentRepository;
import com.medisalud.application.validation.AppointmentValidator;
import com.medisalud.domain.exception.AppointmentConflictException;
import com.medisalud.domain.exception.BusinessRuleException;
import com.medisalud.domain.exception.ResourceNotFoundException;
import com.medisalud.domain.model.Appointment;
import com.medisalud.domain.model.AppointmentStatus;
import com.medisalud.test.TestFixtures;
import io.micrometer.core.instrument.Counter;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.transaction.reactive.TransactionalOperator;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.time.Clock;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AppointmentServiceTest {
    
    @Mock
    private AppointmentRepository appointmentRepository;
    @Mock
    private PenaltyUseCase penaltyService;
    @Mock
    private AppointmentValidator appointmentValidator;
    
    private TimeSlotApplicationService timeSlotService;
    private AppointmentService appointmentService;
    
    private UUID patientId;
    private UUID doctorId;
    
    @BeforeEach
    void setUp() {
        Clock clock = Clock.systemDefaultZone();
        timeSlotService = new TimeSlotApplicationService(TestFixtures.createMockHolidayChecker(), clock);
        
        TransactionalOperator mockTransactionalOperator = TestFixtures.createMockTransactionalOperator();
        Counter mockCreatedCounter = TestFixtures.createMockCounter();
        Counter mockCancelledCounter = TestFixtures.createMockCounter();
        Counter mockRescheduledCounter = TestFixtures.createMockCounter();
        
        appointmentService = new AppointmentService(
                appointmentRepository, timeSlotService, penaltyService, appointmentValidator, 
                mockTransactionalOperator, mockCreatedCounter, mockCancelledCounter, mockRescheduledCounter, clock);
        
        patientId = UUID.randomUUID();
        doctorId = UUID.randomUUID();
    }
    
    @Nested
    @DisplayName("RF-03: Reserva de Citas")
    class CreateAppointmentTests {
        
        @Test
        @DisplayName("Debe crear una cita exitosamente")
        void shouldCreateAppointmentSuccessfully() {
            LocalDateTime dateTime = getNextValidWeekdayAt(10, 0);
            Appointment savedAppointment = createAppointment(dateTime);
            
            when(appointmentValidator.validateForCreation(eq(patientId), eq(doctorId), eq(dateTime)))
                    .thenReturn(Mono.empty());
            when(appointmentRepository.save(any(Appointment.class))).thenReturn(Mono.just(savedAppointment));
            
            StepVerifier.create(appointmentService.createAppointment(patientId, doctorId, dateTime))
                    .expectNextMatches(apt -> apt.getStatus() == AppointmentStatus.PROGRAMADA)
                    .verifyComplete();
        }
        
        @Test
        @DisplayName("Debe fallar si el paciente no existe")
        void shouldFailIfPatientNotFound() {
            LocalDateTime dateTime = getNextValidWeekdayAt(10, 0);
            
            when(appointmentValidator.validateForCreation(eq(patientId), eq(doctorId), eq(dateTime)))
                    .thenReturn(Mono.error(new ResourceNotFoundException("Paciente", patientId.toString())));
            
            StepVerifier.create(appointmentService.createAppointment(patientId, doctorId, dateTime))
                    .expectError(ResourceNotFoundException.class)
                    .verify();
        }
        
        @Test
        @DisplayName("Debe fallar si el médico no existe")
        void shouldFailIfDoctorNotFound() {
            LocalDateTime dateTime = getNextValidWeekdayAt(10, 0);
            
            when(appointmentValidator.validateForCreation(eq(patientId), eq(doctorId), eq(dateTime)))
                    .thenReturn(Mono.error(new ResourceNotFoundException("Médico", doctorId.toString())));
            
            StepVerifier.create(appointmentService.createAppointment(patientId, doctorId, dateTime))
                    .expectError(ResourceNotFoundException.class)
                    .verify();
        }
    }
    
    @Nested
    @DisplayName("RN-02: No Duplicidad de Citas")
    class DoctorAvailabilityTests {
        
        @Test
        @DisplayName("Debe rechazar cita si el médico ya tiene una cita en ese horario")
        void shouldRejectIfDoctorHasAppointmentAtSameTime() {
            LocalDateTime dateTime = getNextValidWeekdayAt(10, 0);
            
            when(appointmentValidator.validateForCreation(eq(patientId), eq(doctorId), eq(dateTime)))
                    .thenReturn(Mono.error(new AppointmentConflictException(
                            "El médico ya tiene una cita programada en este horario")));
            
            StepVerifier.create(appointmentService.createAppointment(patientId, doctorId, dateTime))
                    .expectError(AppointmentConflictException.class)
                    .verify();
        }
    }
    
    @Nested
    @DisplayName("RN-04: Conflicto de Paciente")
    class PatientConflictTests {
        
        @Test
        @DisplayName("Debe rechazar si el paciente ya tiene cita con el mismo médico en ese horario")
        void shouldRejectIfPatientHasAppointmentWithSameDoctorAtSameTime() {
            LocalDateTime dateTime = getNextValidWeekdayAt(10, 0);
            
            when(appointmentValidator.validateForCreation(eq(patientId), eq(doctorId), eq(dateTime)))
                    .thenReturn(Mono.error(new AppointmentConflictException(
                            "El paciente ya tiene una cita programada con este médico en este horario")));
            
            StepVerifier.create(appointmentService.createAppointment(patientId, doctorId, dateTime))
                    .expectError(AppointmentConflictException.class)
                    .verify();
        }
        
        @Test
        @DisplayName("RN-04: Debe permitir que un paciente tenga citas con DIFERENTES médicos al mismo tiempo")
        void shouldAllowPatientToHaveAppointmentsWithDifferentDoctorsAtSameTime() {
            LocalDateTime dateTime = getNextValidWeekdayAt(10, 0);
            UUID doctor1Id = UUID.randomUUID();
            UUID doctor2Id = UUID.randomUUID();
            
            Appointment appointment1 = Appointment.builder()
                    .id(UUID.randomUUID())
                    .patientId(patientId)
                    .doctorId(doctor1Id)
                    .dateTime(dateTime)
                    .status(AppointmentStatus.PROGRAMADA)
                    .createdAt(LocalDateTime.now())
                    .updatedAt(LocalDateTime.now())
                    .build();
            
            Appointment appointment2 = Appointment.builder()
                    .id(UUID.randomUUID())
                    .patientId(patientId)
                    .doctorId(doctor2Id)
                    .dateTime(dateTime)
                    .status(AppointmentStatus.PROGRAMADA)
                    .createdAt(LocalDateTime.now())
                    .updatedAt(LocalDateTime.now())
                    .build();
            
            when(appointmentValidator.validateForCreation(eq(patientId), eq(doctor1Id), eq(dateTime)))
                    .thenReturn(Mono.empty());
            when(appointmentRepository.save(any(Appointment.class)))
                    .thenReturn(Mono.just(appointment1))
                    .thenReturn(Mono.just(appointment2));
            
            when(appointmentValidator.validateForCreation(eq(patientId), eq(doctor2Id), eq(dateTime)))
                    .thenReturn(Mono.empty());
            
            StepVerifier.create(appointmentService.createAppointment(patientId, doctor1Id, dateTime))
                    .expectNextMatches(apt -> apt.getDoctorId().equals(doctor1Id))
                    .verifyComplete();
            
            StepVerifier.create(appointmentService.createAppointment(patientId, doctor2Id, dateTime))
                    .expectNextMatches(apt -> apt.getDoctorId().equals(doctor2Id))
                    .verifyComplete();
        }
    }
    
    @Nested
    @DisplayName("RF-04: Consulta de Citas Disponibles")
    class AvailableSlotsTests {
        
        @Test
        @DisplayName("Debe retornar franjas disponibles excluyendo las ocupadas (fecha futura)")
        void shouldReturnAvailableSlotsExcludingOccupied() {
            LocalDate date = LocalDate.now().plusDays(7);
            if (date.getDayOfWeek() == DayOfWeek.SUNDAY) {
                date = date.plusDays(1);
            }
            if (date.getDayOfWeek() == DayOfWeek.SATURDAY) {
                date = date.plusDays(2);
            }
            LocalDateTime occupiedSlot = LocalDateTime.of(date, LocalTime.of(10, 0));
            final LocalDate testDate = date;
            
            Appointment existingAppointment = Appointment.builder()
                    .id(UUID.randomUUID())
                    .doctorId(doctorId)
                    .dateTime(occupiedSlot)
                    .status(AppointmentStatus.PROGRAMADA)
                    .build();
            
            when(appointmentValidator.validateDateRange(eq(testDate), eq(testDate))).thenReturn(Mono.empty());
            when(appointmentValidator.validateDoctorExists(doctorId)).thenReturn(Mono.empty());
            when(appointmentRepository.findByDoctorIdAndDateTimeBetween(eq(doctorId), any(), any()))
                    .thenReturn(Flux.just(existingAppointment));
            
            StepVerifier.create(appointmentService.getAvailableSlots(doctorId, testDate, testDate))
                    .expectNextCount(19)
                    .verifyComplete();
        }
    }
    
    @Nested
    @DisplayName("RF-05: Cancelación de Citas")
    class CancelAppointmentTests {
        
        @Test
        @DisplayName("Debe cancelar una cita programada exitosamente")
        void shouldCancelScheduledAppointmentSuccessfully() {
            UUID appointmentId = UUID.randomUUID();
            LocalDateTime futureDateTime = LocalDateTime.now().plusDays(1);
            Appointment appointment = createAppointmentWithId(appointmentId, futureDateTime);
            Appointment cancelledAppointment = createCancelledAppointment(appointmentId, futureDateTime);
            
            when(appointmentRepository.findById(appointmentId)).thenReturn(Mono.just(appointment));
            when(penaltyService.applyPenalty(any(Appointment.class))).thenReturn(Mono.empty());
            when(appointmentRepository.update(any(Appointment.class))).thenReturn(Mono.just(cancelledAppointment));
            
            StepVerifier.create(appointmentService.cancelAppointment(appointmentId))
                    .expectNextMatches(apt -> apt.getStatus() == AppointmentStatus.CANCELADA)
                    .verifyComplete();
        }
        
        @Test
        @DisplayName("Debe retornar la misma cita si ya está cancelada (idempotencia)")
        void shouldReturnSameAppointmentIfAlreadyCancelled() {
            UUID appointmentId = UUID.randomUUID();
            Appointment appointment = createCancelledAppointment(appointmentId, LocalDateTime.now().plusDays(1));
            
            when(appointmentRepository.findById(appointmentId)).thenReturn(Mono.just(appointment));
            
            StepVerifier.create(appointmentService.cancelAppointment(appointmentId))
                    .expectNextMatches(apt -> apt.getStatus() == AppointmentStatus.CANCELADA && apt.getId().equals(appointmentId))
                    .verifyComplete();
        }
    }
    
    @Nested
    @DisplayName("RN-06: Reprogramación")
    class RescheduleAppointmentTests {
        
        @Test
        @DisplayName("Debe reprogramar una cita exitosamente")
        void shouldRescheduleAppointmentSuccessfully() {
            UUID appointmentId = UUID.randomUUID();
            LocalDateTime originalDateTime = getNextValidWeekdayAt(10, 0);
            LocalDateTime newDateTime = getNextValidWeekdayAt(14, 0);
            
            Appointment originalAppointment = createAppointmentWithId(appointmentId, originalDateTime);
            Appointment cancelledAppointment = createCancelledAppointment(appointmentId, originalDateTime);
            Appointment newAppointment = createAppointment(newDateTime);
            
            when(appointmentRepository.findById(appointmentId)).thenReturn(Mono.just(originalAppointment));
            when(appointmentValidator.validateForReschedule(eq(patientId), eq(doctorId), eq(newDateTime)))
                    .thenReturn(Mono.empty());
            when(penaltyService.applyPenalty(any(Appointment.class))).thenReturn(Mono.empty());
            when(appointmentRepository.update(any(Appointment.class))).thenReturn(Mono.just(cancelledAppointment));
            when(appointmentRepository.save(any(Appointment.class))).thenReturn(Mono.just(newAppointment));
            
            StepVerifier.create(appointmentService.rescheduleAppointment(appointmentId, newDateTime))
                    .expectNextMatches(apt -> apt.getDateTime().equals(newDateTime))
                    .verifyComplete();
        }
    }
    
    @Nested
    @DisplayName("RN-05: Penalización por Cancelación Tardía")
    class LateCancellationPenaltyTests {
        
        @Test
        @DisplayName("Debe aplicar penalización cuando se cancela exactamente a 2 horas de la cita")
        void shouldApplyPenaltyWhenCancellingExactlyAt2Hours() {
            UUID appointmentId = UUID.randomUUID();
            LocalDateTime appointmentDateTime = LocalDateTime.now().plusHours(2);
            Appointment appointment = createAppointmentWithId(appointmentId, appointmentDateTime);
            Appointment cancelledAppointment = createCancelledAppointment(appointmentId, appointmentDateTime);
            
            when(appointmentRepository.findById(appointmentId)).thenReturn(Mono.just(appointment));
            when(penaltyService.applyPenalty(any(Appointment.class))).thenReturn(Mono.empty());
            when(appointmentRepository.update(any(Appointment.class))).thenReturn(Mono.just(cancelledAppointment));
            
            StepVerifier.create(appointmentService.cancelAppointment(appointmentId))
                    .expectNextMatches(apt -> apt.getStatus() == AppointmentStatus.CANCELADA)
                    .verifyComplete();
            
            verify(penaltyService).applyPenalty(any(Appointment.class));
        }
        
        @Test
        @DisplayName("Debe aplicar penalización cuando se cancela con menos de 2 horas")
        void shouldApplyPenaltyWhenCancellingWithLessThan2Hours() {
            UUID appointmentId = UUID.randomUUID();
            LocalDateTime appointmentDateTime = LocalDateTime.now().plusHours(1).plusMinutes(59);
            Appointment appointment = createAppointmentWithId(appointmentId, appointmentDateTime);
            Appointment cancelledAppointment = createCancelledAppointment(appointmentId, appointmentDateTime);
            
            when(appointmentRepository.findById(appointmentId)).thenReturn(Mono.just(appointment));
            when(penaltyService.applyPenalty(any(Appointment.class))).thenReturn(Mono.empty());
            when(appointmentRepository.update(any(Appointment.class))).thenReturn(Mono.just(cancelledAppointment));
            
            StepVerifier.create(appointmentService.cancelAppointment(appointmentId))
                    .expectNextMatches(apt -> apt.getStatus() == AppointmentStatus.CANCELADA)
                    .verifyComplete();
            
            verify(penaltyService).applyPenalty(any(Appointment.class));
        }
    }
    
    @Nested
    @DisplayName("Marcar Cita como Atendida")
    class MarkAsAttendedTests {
        
        @Test
        @DisplayName("Debe marcar una cita programada como atendida")
        void shouldMarkScheduledAppointmentAsAttended() {
            UUID appointmentId = UUID.randomUUID();
            LocalDateTime pastDateTime = LocalDateTime.now().minusHours(1);
            Appointment appointment = createAppointmentWithId(appointmentId, pastDateTime);
            Appointment attendedAppointment = createAttendedAppointment(appointmentId, pastDateTime);
            
            when(appointmentRepository.findById(appointmentId)).thenReturn(Mono.just(appointment));
            when(appointmentRepository.update(any(Appointment.class))).thenReturn(Mono.just(attendedAppointment));
            
            StepVerifier.create(appointmentService.markAsAttended(appointmentId))
                    .expectNextMatches(apt -> apt.getStatus() == AppointmentStatus.ATENDIDA)
                    .verifyComplete();
        }
        
        @Test
        @DisplayName("Debe fallar al marcar como atendida una cita cancelada")
        void shouldFailToMarkCancelledAppointmentAsAttended() {
            UUID appointmentId = UUID.randomUUID();
            Appointment appointment = createCancelledAppointment(appointmentId, LocalDateTime.now().minusHours(1));
            
            when(appointmentRepository.findById(appointmentId)).thenReturn(Mono.just(appointment));
            
            StepVerifier.create(appointmentService.markAsAttended(appointmentId))
                    .expectError(BusinessRuleException.class)
                    .verify();
        }
        
        @Test
        @DisplayName("Debe fallar si la cita no existe")
        void shouldFailIfAppointmentNotFound() {
            UUID appointmentId = UUID.randomUUID();
            
            when(appointmentRepository.findById(appointmentId)).thenReturn(Mono.empty());
            
            StepVerifier.create(appointmentService.markAsAttended(appointmentId))
                    .expectError(ResourceNotFoundException.class)
                    .verify();
        }
    }
    
    private Appointment createAppointment(LocalDateTime dateTime) {
        return Appointment.builder()
                .id(UUID.randomUUID())
                .patientId(patientId)
                .doctorId(doctorId)
                .dateTime(dateTime)
                .status(AppointmentStatus.PROGRAMADA)
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();
    }
    
    private Appointment createAppointmentWithId(UUID id, LocalDateTime dateTime) {
        return Appointment.builder()
                .id(id)
                .patientId(patientId)
                .doctorId(doctorId)
                .dateTime(dateTime)
                .status(AppointmentStatus.PROGRAMADA)
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();
    }
    
    private Appointment createCancelledAppointment(UUID id, LocalDateTime dateTime) {
        return Appointment.builder()
                .id(id)
                .patientId(patientId)
                .doctorId(doctorId)
                .dateTime(dateTime)
                .status(AppointmentStatus.CANCELADA)
                .cancellationDateTime(LocalDateTime.now())
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();
    }
    
    private Appointment createAttendedAppointment(UUID id, LocalDateTime dateTime) {
        return Appointment.builder()
                .id(id)
                .patientId(patientId)
                .doctorId(doctorId)
                .dateTime(dateTime)
                .status(AppointmentStatus.ATENDIDA)
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();
    }
    
    private static LocalDateTime getNextValidWeekdayAt(int hour, int minute) {
        LocalDate date = LocalDate.now().plusDays(7);
        while (date.getDayOfWeek() == DayOfWeek.SATURDAY || date.getDayOfWeek() == DayOfWeek.SUNDAY) {
            date = date.plusDays(1);
        }
        return LocalDateTime.of(date, LocalTime.of(hour, minute));
    }
}
