package com.medisalud.domain.service;

import com.medisalud.application.service.TimeSlotApplicationService;
import com.medisalud.domain.exception.InvalidTimeSlotException;
import com.medisalud.domain.model.TimeSlot;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import reactor.test.StepVerifier;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class TimeSlotServiceTest {
    
    private TimeSlotApplicationService timeSlotService;
    private HolidayChecker holidayService;
    
    @BeforeEach
    void setUp() {
        holidayService = com.medisalud.test.TestFixtures.createMockHolidayChecker();
        timeSlotService = new TimeSlotApplicationService(holidayService);
    }
    
    @Nested
    @DisplayName("RN-01: Franjas Horarias de Atención")
    class TimeSlotGenerationTests {
        
        @Test
        @DisplayName("Debe generar 20 franjas de 30 minutos para un día laboral (Lunes-Viernes)")
        void shouldGenerate20SlotsForWeekday() {
            LocalDate monday = getNextWeekday(java.time.DayOfWeek.MONDAY);
            
            List<TimeSlot> slots = timeSlotService.generateSlotsForDate(monday);
            
            assertEquals(20, slots.size());
            assertEquals(LocalTime.of(8, 0), slots.get(0).getStartTime().toLocalTime());
            assertEquals(LocalTime.of(17, 30), slots.get(19).getStartTime().toLocalTime());
        }
        
        @Test
        @DisplayName("Debe generar 10 franjas de 30 minutos para un sábado (08:00-13:00)")
        void shouldGenerate10SlotsForSaturday() {
            LocalDate saturday = getNextWeekday(java.time.DayOfWeek.SATURDAY);
            
            List<TimeSlot> slots = timeSlotService.generateSlotsForDate(saturday);
            
            assertEquals(10, slots.size());
            assertEquals(LocalTime.of(8, 0), slots.get(0).getStartTime().toLocalTime());
            assertEquals(LocalTime.of(12, 30), slots.get(9).getStartTime().toLocalTime());
        }
        
        @Test
        @DisplayName("No debe generar franjas para domingo")
        void shouldGenerateNoSlotsForSunday() {
            LocalDate sunday = getNextWeekday(java.time.DayOfWeek.SUNDAY);
            
            List<TimeSlot> slots = timeSlotService.generateSlotsForDate(sunday);
            
            assertTrue(slots.isEmpty());
        }
        
        @Test
        @DisplayName("Debe generar franjas para un rango de fechas")
        void shouldGenerateSlotsForDateRange() {
            LocalDate monday = getNextWeekday(java.time.DayOfWeek.MONDAY);
            LocalDate wednesday = monday.plusDays(2);
            
            List<TimeSlot> slots = timeSlotService.generateSlotsForDateRange(monday, wednesday);
            
            assertTrue(slots.size() >= 40 && slots.size() <= 60);
        }
    }
    
    @Nested
    @DisplayName("Validación de Franjas Horarias")
    class TimeSlotValidationTests {
        
        @Test
        @DisplayName("Debe rechazar citas en domingo")
        void shouldRejectSundayAppointments() {
            LocalDateTime sunday = LocalDateTime.of(2025, 12, 21, 10, 0);
            
            StepVerifier.create(timeSlotService.validateTimeSlot(sunday))
                    .expectErrorMatches(throwable -> 
                            throwable instanceof InvalidTimeSlotException &&
                            throwable.getMessage().contains("domingo"))
                    .verify();
        }
        
        @Test
        @DisplayName("Debe rechazar citas fuera del horario laboral en días de semana")
        void shouldRejectOutOfHoursWeekdayAppointments() {
            LocalDateTime earlyMorning = LocalDateTime.of(2025, 12, 16, 7, 0);
            LocalDateTime lateEvening = LocalDateTime.of(2025, 12, 16, 19, 0);
            
            StepVerifier.create(timeSlotService.validateTimeSlot(earlyMorning))
                    .expectError(InvalidTimeSlotException.class)
                    .verify();
            
            StepVerifier.create(timeSlotService.validateTimeSlot(lateEvening))
                    .expectError(InvalidTimeSlotException.class)
                    .verify();
        }
        
        @Test
        @DisplayName("Debe rechazar citas fuera del horario de sábado")
        void shouldRejectOutOfHoursSaturdayAppointments() {
            LocalDateTime afternoon = LocalDateTime.of(2025, 12, 20, 14, 0);
            
            StepVerifier.create(timeSlotService.validateTimeSlot(afternoon))
                    .expectError(InvalidTimeSlotException.class)
                    .verify();
        }
        
        @Test
        @DisplayName("Debe rechazar citas que no están en franjas de 30 minutos")
        void shouldRejectNon30MinuteSlots() {
            LocalDateTime invalidTime = LocalDateTime.of(2025, 12, 16, 10, 15);
            
            StepVerifier.create(timeSlotService.validateTimeSlot(invalidTime))
                    .expectErrorMatches(throwable -> 
                            throwable instanceof InvalidTimeSlotException &&
                            throwable.getMessage().contains("30 minutos"))
                    .verify();
        }
        
        @Test
        @DisplayName("Debe aceptar citas válidas en horario laboral")
        void shouldAcceptValidWeekdayAppointments() {
            LocalDateTime validTime = LocalDateTime.of(2025, 12, 16, 10, 30);
            
            StepVerifier.create(timeSlotService.validateTimeSlot(validTime))
                    .verifyComplete();
        }
        
        @Test
        @DisplayName("Debe aceptar citas válidas en sábado")
        void shouldAcceptValidSaturdayAppointments() {
            LocalDateTime validTime = LocalDateTime.of(2025, 12, 20, 10, 0);
            
            StepVerifier.create(timeSlotService.validateTimeSlot(validTime))
                    .verifyComplete();
        }
        
        @Test
        @DisplayName("RN-01: Debe rechazar citas en días festivos")
        void shouldRejectHolidayAppointments() {
            LocalDate holiday = LocalDate.of(2025, 12, 25);
            HolidayChecker mockHolidayChecker = mock(HolidayChecker.class);
            Set<LocalDate> holidays = new HashSet<>();
            holidays.add(holiday);
            when(mockHolidayChecker.isHoliday(holiday)).thenReturn(true);
            when(mockHolidayChecker.getHolidays()).thenReturn(holidays);
            
            TimeSlotApplicationService serviceWithHoliday = new TimeSlotApplicationService(mockHolidayChecker);
            LocalDateTime holidayTime = LocalDateTime.of(2025, 12, 25, 10, 0);
            
            StepVerifier.create(serviceWithHoliday.validateTimeSlot(holidayTime))
                    .expectErrorMatches(throwable -> 
                            throwable instanceof InvalidTimeSlotException &&
                            throwable.getMessage().contains("festivo"))
                    .verify();
        }
        
        @Test
        @DisplayName("Debe rechazar citas con menos de MIN_ADVANCE_HOURS de antelación")
        void shouldRejectAppointmentsWithInsufficientAdvance() {
            LocalDateTime now = LocalDateTime.now();
            LocalDateTime tooSoon = now.plusMinutes(30);
            
            if (tooSoon.getMinute() % 30 != 0) {
                tooSoon = tooSoon.withMinute(tooSoon.getMinute() < 30 ? 0 : 30).withSecond(0).withNano(0);
            }
            
            StepVerifier.create(timeSlotService.validateTimeSlot(tooSoon))
                    .expectError(InvalidTimeSlotException.class)
                    .verify();
        }
    }
    
    @Nested
    @DisplayName("Normalización de Franjas")
    class TimeSlotNormalizationTests {
        
        @Test
        @DisplayName("Debe normalizar minutos a la franja de 30 minutos más cercana hacia abajo")
        void shouldNormalizeToNearestSlot() {
            LocalDateTime time = LocalDateTime.of(2024, 12, 16, 10, 45, 30);
            
            LocalDateTime normalized = timeSlotService.normalizeToSlotStart(time);
            
            assertEquals(LocalDateTime.of(2024, 12, 16, 10, 30, 0), normalized);
        }
        
        @Test
        @DisplayName("Debe mantener tiempos que ya están en franjas válidas")
        void shouldKeepValidSlotTimes() {
            LocalDate nextMonday = getNextWeekday(java.time.DayOfWeek.MONDAY);
            LocalDateTime time = nextMonday.atTime(10, 30, 0);
            
            LocalDateTime normalized = timeSlotService.normalizeToSlotStart(time);
            
            assertEquals(time, normalized);
        }
    }
    
    private static LocalDate getNextWeekday(java.time.DayOfWeek targetDay) {
        LocalDate today = LocalDate.now();
        int daysUntilTarget = (targetDay.getValue() - today.getDayOfWeek().getValue() + 7) % 7;
        if (daysUntilTarget == 0) {
            daysUntilTarget = 7;
        }
        return today.plusDays(daysUntilTarget);
    }
}
