package com.medisalud.domain.service;

import com.medisalud.application.port.output.PenaltyRepository;
import com.medisalud.application.service.PenaltyApplicationService;
import com.medisalud.domain.exception.PatientPenalizedException;
import com.medisalud.domain.model.Appointment;
import com.medisalud.domain.model.AppointmentStatus;
import com.medisalud.domain.model.Penalty;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.time.LocalDateTime;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class PenaltyApplicationServiceTest {
    
    @Mock
    private PenaltyRepository penaltyRepository;
    
    @Mock
    private io.micrometer.core.instrument.Counter penaltiesAppliedCounter;
    
    @Mock
    private io.micrometer.core.instrument.Counter lateCancellationsCounter;
    
    private PenaltyApplicationService penaltyService;
    
    @BeforeEach
    void setUp() {
        java.time.Clock fixedClock = java.time.Clock.fixed(
                java.time.Instant.now(), java.time.ZoneId.systemDefault());
        penaltyService = new PenaltyApplicationService(penaltyRepository, penaltiesAppliedCounter, lateCancellationsCounter, fixedClock);
    }
    
    @Nested
    @DisplayName("RN-05: Penalización por Cancelación Tardía")
    class LateCancellationPenaltyTests {
        
        @Test
        @DisplayName("Debe aplicar penalización si la cancelación es con menos de 2 horas de antelación")
        void shouldApplyPenaltyForLateCancellation() {
            Appointment appointment = createAppointment(LocalDateTime.now().plusMinutes(30));
            
            StepVerifier.create(penaltyService.shouldApplyPenalty(appointment))
                    .expectNext(true)
                    .verifyComplete();
        }
        
        @Test
        @DisplayName("No debe aplicar penalización si la cancelación es con más de 2 horas de antelación")
        void shouldNotApplyPenaltyForEarlyCancellation() {
            Appointment appointment = createAppointment(LocalDateTime.now().plusHours(3));
            
            StepVerifier.create(penaltyService.shouldApplyPenalty(appointment))
                    .expectNext(false)
                    .verifyComplete();
        }
        
        @Test
        @DisplayName("Debe aplicar penalización si la cancelación es exactamente a 2 horas (límite)")
        void shouldApplyPenaltyForExactly2HoursCancellation() {
            Appointment appointment = createAppointment(LocalDateTime.now().plusHours(2));
            
            StepVerifier.create(penaltyService.shouldApplyPenalty(appointment))
                    .expectNext(true)
                    .verifyComplete();
        }
        
        @Test
        @DisplayName("No debe aplicar penalización si la cancelación es a 2 horas y 1 minuto")
        void shouldNotApplyPenaltyFor2HoursAnd1MinuteCancellation() {
            Appointment appointment = createAppointment(LocalDateTime.now().plusHours(2).plusMinutes(1));
            
            StepVerifier.create(penaltyService.shouldApplyPenalty(appointment))
                    .expectNext(false)
                    .verifyComplete();
        }
        
        @Test
        @DisplayName("Debe guardar la penalización cuando corresponde")
        void shouldSavePenaltyWhenApplicable() {
            Appointment appointment = createAppointment(LocalDateTime.now().plusMinutes(30));
            Penalty savedPenalty = Penalty.builder()
                    .id(UUID.randomUUID())
                    .patientId(appointment.getPatientId())
                    .appointmentId(appointment.getId())
                    .penaltyDateTime(LocalDateTime.now())
                    .reason("Cancelación tardía (menos de 2 horas de antelación)")
                    .build();
            
            when(penaltyRepository.save(any(Penalty.class))).thenReturn(Mono.just(savedPenalty));
            
            StepVerifier.create(penaltyService.applyPenalty(appointment))
                    .expectNextMatches(penalty -> penalty.getPatientId().equals(appointment.getPatientId()))
                    .verifyComplete();
            
            verify(penaltyRepository, times(1)).save(any(Penalty.class));
        }
        
        @Test
        @DisplayName("No debe guardar penalización si la cancelación es temprana")
        void shouldNotSavePenaltyForEarlyCancellation() {
            Appointment appointment = createAppointment(LocalDateTime.now().plusHours(5));
            
            StepVerifier.create(penaltyService.applyPenalty(appointment))
                    .verifyComplete();
            
            verify(penaltyRepository, never()).save(any(Penalty.class));
        }
    }
    
    @Nested
    @DisplayName("Verificación de Paciente Penalizado")
    class PatientPenaltyCheckTests {
        
        @Test
        @DisplayName("Debe permitir agendar si el paciente tiene menos de 3 penalizaciones")
        void shouldAllowSchedulingWithLessThan3Penalties() {
            UUID patientId = UUID.randomUUID();
            when(penaltyRepository.countByPatientIdAndPenaltyDateTimeAfter(eq(patientId), any(LocalDateTime.class)))
                    .thenReturn(Mono.just(2L));
            
            StepVerifier.create(penaltyService.checkPatientCanSchedule(patientId))
                    .verifyComplete();
        }
        
        @Test
        @DisplayName("Debe bloquear agendamiento si el paciente tiene 3 o más penalizaciones")
        void shouldBlockSchedulingWith3OrMorePenalties() {
            UUID patientId = UUID.randomUUID();
            when(penaltyRepository.countByPatientIdAndPenaltyDateTimeAfter(eq(patientId), any(LocalDateTime.class)))
                    .thenReturn(Mono.just(3L));
            
            StepVerifier.create(penaltyService.checkPatientCanSchedule(patientId))
                    .expectError(PatientPenalizedException.class)
                    .verify();
        }
        
        @Test
        @DisplayName("Debe permitir agendar si el paciente no tiene penalizaciones")
        void shouldAllowSchedulingWithNoPenalties() {
            UUID patientId = UUID.randomUUID();
            when(penaltyRepository.countByPatientIdAndPenaltyDateTimeAfter(eq(patientId), any(LocalDateTime.class)))
                    .thenReturn(Mono.just(0L));
            
            StepVerifier.create(penaltyService.checkPatientCanSchedule(patientId))
                    .verifyComplete();
        }
        
        @Test
        @DisplayName("Debe bloquear agendamiento exactamente en la 3ra penalización (límite exacto)")
        void shouldBlockSchedulingAtExactly3rdPenalty() {
            UUID patientId = UUID.randomUUID();
            when(penaltyRepository.countByPatientIdAndPenaltyDateTimeAfter(eq(patientId), any(LocalDateTime.class)))
                    .thenReturn(Mono.just(3L));
            
            StepVerifier.create(penaltyService.checkPatientCanSchedule(patientId))
                    .expectErrorMatches(e -> e instanceof PatientPenalizedException &&
                            e.getMessage().contains("3 penalizaciones"))
                    .verify();
        }
        
        @Test
        @DisplayName("Debe bloquear agendamiento con más de 3 penalizaciones")
        void shouldBlockSchedulingWithMoreThan3Penalties() {
            UUID patientId = UUID.randomUUID();
            when(penaltyRepository.countByPatientIdAndPenaltyDateTimeAfter(eq(patientId), any(LocalDateTime.class)))
                    .thenReturn(Mono.just(5L));
            
            StepVerifier.create(penaltyService.checkPatientCanSchedule(patientId))
                    .expectError(PatientPenalizedException.class)
                    .verify();
        }
    }
    
    private Appointment createAppointment(LocalDateTime dateTime) {
        return Appointment.builder()
                .id(UUID.randomUUID())
                .patientId(UUID.randomUUID())
                .doctorId(UUID.randomUUID())
                .dateTime(dateTime)
                .status(AppointmentStatus.PROGRAMADA)
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();
    }
}
