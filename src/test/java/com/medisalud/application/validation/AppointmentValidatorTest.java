package com.medisalud.application.validation;

import com.medisalud.application.port.output.AppointmentRepository;
import com.medisalud.application.port.output.DoctorRepository;
import com.medisalud.application.port.output.PatientRepository;
import com.medisalud.domain.exception.AppointmentConflictException;
import com.medisalud.domain.exception.BusinessRuleException;
import com.medisalud.domain.exception.PatientPenalizedException;
import com.medisalud.domain.exception.ResourceNotFoundException;
import com.medisalud.domain.model.AppointmentStatus;
import com.medisalud.application.port.input.PenaltyUseCase;
import com.medisalud.application.service.TimeSlotApplicationService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
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
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AppointmentValidatorTest {

    @Mock
    private AppointmentRepository appointmentRepository;
    @Mock
    private DoctorRepository doctorRepository;
    @Mock
    private PatientRepository patientRepository;
    @Mock
    private PenaltyUseCase penaltyService;

    private TimeSlotApplicationService timeSlotService;
    private AppointmentValidator appointmentValidator;

    private UUID patientId;
    private UUID doctorId;

    @BeforeEach
    void setUp() {
        timeSlotService = new TimeSlotApplicationService(
                com.medisalud.test.TestFixtures.createMockHolidayChecker(), Clock.systemDefaultZone());
        appointmentValidator = new AppointmentValidator(
                appointmentRepository, doctorRepository, patientRepository,
                timeSlotService, penaltyService);

        patientId = UUID.randomUUID();
        doctorId = UUID.randomUUID();
    }
    
    private static LocalDateTime getNextValidWeekdayAt(int hour, int minute) {
        LocalDate date = LocalDate.now().plusDays(7);
        while (date.getDayOfWeek() == DayOfWeek.SATURDAY || date.getDayOfWeek() == DayOfWeek.SUNDAY) {
            date = date.plusDays(1);
        }
        return LocalDateTime.of(date, LocalTime.of(hour, minute));
    }

    @Nested
    @DisplayName("Validación para Creación de Citas")
    class ValidateForCreationTests {

        @Test
        @DisplayName("Debe pasar validación cuando todo es correcto")
        void shouldPassValidationWhenAllCorrect() {
            LocalDateTime dateTime = getNextValidWeekdayAt(10, 0);

            when(patientRepository.existsById(patientId)).thenReturn(Mono.just(true));
            when(doctorRepository.existsById(doctorId)).thenReturn(Mono.just(true));
            when(penaltyService.checkPatientCanSchedule(patientId)).thenReturn(Mono.empty());
            when(appointmentRepository.existsByDoctorIdAndDateTimeAndStatus(
                    eq(doctorId), eq(dateTime), eq(AppointmentStatus.PROGRAMADA)))
                    .thenReturn(Mono.just(false));
            when(appointmentRepository.existsByPatientIdAndDoctorIdAndDateTimeAndStatus(
                    eq(patientId), eq(doctorId), eq(dateTime), eq(AppointmentStatus.PROGRAMADA)))
                    .thenReturn(Mono.just(false));

            StepVerifier.create(appointmentValidator.validateForCreation(patientId, doctorId, dateTime))
                    .verifyComplete();
        }

        @Test
        @DisplayName("Debe fallar si el paciente no existe")
        void shouldFailIfPatientNotFound() {
            LocalDateTime dateTime = getNextValidWeekdayAt(10, 0);

            when(patientRepository.existsById(patientId)).thenReturn(Mono.just(false));
            when(doctorRepository.existsById(doctorId)).thenReturn(Mono.just(true));

            StepVerifier.create(appointmentValidator.validateForCreation(patientId, doctorId, dateTime))
                    .expectError(ResourceNotFoundException.class)
                    .verify();
        }

        @Test
        @DisplayName("Debe fallar si el médico no existe")
        void shouldFailIfDoctorNotFound() {
            LocalDateTime dateTime = getNextValidWeekdayAt(10, 0);

            when(patientRepository.existsById(patientId)).thenReturn(Mono.just(true));
            when(doctorRepository.existsById(doctorId)).thenReturn(Mono.just(false));

            StepVerifier.create(appointmentValidator.validateForCreation(patientId, doctorId, dateTime))
                    .expectError(ResourceNotFoundException.class)
                    .verify();
        }

        @Test
        @DisplayName("Debe fallar si el paciente está penalizado")
        void shouldFailIfPatientIsPenalized() {
            LocalDateTime dateTime = getNextValidWeekdayAt(10, 0);

            when(patientRepository.existsById(patientId)).thenReturn(Mono.just(true));
            when(doctorRepository.existsById(doctorId)).thenReturn(Mono.just(true));
            when(penaltyService.checkPatientCanSchedule(patientId))
                    .thenReturn(Mono.error(new PatientPenalizedException(3)));

            StepVerifier.create(appointmentValidator.validateForCreation(patientId, doctorId, dateTime))
                    .expectError(PatientPenalizedException.class)
                    .verify();
        }

        @Test
        @DisplayName("Debe fallar si el médico ya tiene cita en ese horario")
        void shouldFailIfDoctorHasAppointmentAtSameTime() {
            LocalDateTime dateTime = getNextValidWeekdayAt(10, 0);

            when(patientRepository.existsById(patientId)).thenReturn(Mono.just(true));
            when(doctorRepository.existsById(doctorId)).thenReturn(Mono.just(true));
            when(penaltyService.checkPatientCanSchedule(patientId)).thenReturn(Mono.empty());
            when(appointmentRepository.existsByDoctorIdAndDateTimeAndStatus(
                    eq(doctorId), eq(dateTime), eq(AppointmentStatus.PROGRAMADA)))
                    .thenReturn(Mono.just(true));

            StepVerifier.create(appointmentValidator.validateForCreation(patientId, doctorId, dateTime))
                    .expectError(AppointmentConflictException.class)
                    .verify();
        }

        @Test
        @DisplayName("Debe fallar si el paciente ya tiene cita con el mismo médico en ese horario")
        void shouldFailIfPatientHasAppointmentWithSameDoctorAtSameTime() {
            LocalDateTime dateTime = getNextValidWeekdayAt(10, 0);

            when(patientRepository.existsById(patientId)).thenReturn(Mono.just(true));
            when(doctorRepository.existsById(doctorId)).thenReturn(Mono.just(true));
            when(penaltyService.checkPatientCanSchedule(patientId)).thenReturn(Mono.empty());
            when(appointmentRepository.existsByDoctorIdAndDateTimeAndStatus(
                    eq(doctorId), eq(dateTime), eq(AppointmentStatus.PROGRAMADA)))
                    .thenReturn(Mono.just(false));
            when(appointmentRepository.existsByPatientIdAndDoctorIdAndDateTimeAndStatus(
                    eq(patientId), eq(doctorId), eq(dateTime), eq(AppointmentStatus.PROGRAMADA)))
                    .thenReturn(Mono.just(true));

            StepVerifier.create(appointmentValidator.validateForCreation(patientId, doctorId, dateTime))
                    .expectError(AppointmentConflictException.class)
                    .verify();
        }
    }

    @Nested
    @DisplayName("Validación de Rango de Fechas")
    class ValidateDateRangeTests {

        @Test
        @DisplayName("Debe pasar validación con rango válido")
        void shouldPassWithValidDateRange() {
            LocalDate startDate = LocalDate.now().plusDays(7);
            LocalDate endDate = startDate.plusDays(4);

            StepVerifier.create(appointmentValidator.validateDateRange(startDate, endDate))
                    .verifyComplete();
        }

        @Test
        @DisplayName("Debe fallar si fecha inicio es null")
        void shouldFailIfStartDateIsNull() {
            LocalDate endDate = LocalDate.now().plusDays(7);

            StepVerifier.create(appointmentValidator.validateDateRange(null, endDate))
                    .expectError(BusinessRuleException.class)
                    .verify();
        }

        @Test
        @DisplayName("Debe fallar si fecha fin es null")
        void shouldFailIfEndDateIsNull() {
            LocalDate startDate = LocalDate.now().plusDays(7);

            StepVerifier.create(appointmentValidator.validateDateRange(startDate, null))
                    .expectError(BusinessRuleException.class)
                    .verify();
        }

        @Test
        @DisplayName("Debe fallar si fecha inicio es posterior a fecha fin")
        void shouldFailIfStartDateAfterEndDate() {
            LocalDate startDate = LocalDate.now().plusDays(10);
            LocalDate endDate = LocalDate.now().plusDays(5);

            StepVerifier.create(appointmentValidator.validateDateRange(startDate, endDate))
                    .expectErrorMatches(throwable ->
                            throwable instanceof BusinessRuleException &&
                            throwable.getMessage().contains("posterior"))
                    .verify();
        }

        @Test
        @DisplayName("Debe fallar si rango excede 90 días")
        void shouldFailIfDateRangeExceeds90Days() {
            LocalDate startDate = LocalDate.now().plusDays(1);
            LocalDate endDate = startDate.plusDays(100);

            StepVerifier.create(appointmentValidator.validateDateRange(startDate, endDate))
                    .expectErrorMatches(throwable ->
                            throwable instanceof BusinessRuleException &&
                            throwable.getMessage().contains("90"))
                    .verify();
        }
    }

    @Nested
    @DisplayName("Validación de Existencia de Médico")
    class ValidateDoctorExistsTests {

        @Test
        @DisplayName("Debe pasar si el médico existe")
        void shouldPassIfDoctorExists() {
            when(doctorRepository.existsById(doctorId)).thenReturn(Mono.just(true));

            StepVerifier.create(appointmentValidator.validateDoctorExists(doctorId))
                    .verifyComplete();
        }

        @Test
        @DisplayName("Debe fallar si el médico no existe")
        void shouldFailIfDoctorNotFound() {
            when(doctorRepository.existsById(doctorId)).thenReturn(Mono.just(false));

            StepVerifier.create(appointmentValidator.validateDoctorExists(doctorId))
                    .expectError(ResourceNotFoundException.class)
                    .verify();
        }
    }
}
