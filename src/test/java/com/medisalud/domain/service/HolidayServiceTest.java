package com.medisalud.domain.service;

import com.medisalud.application.port.output.HolidayApiPort;
import com.medisalud.application.service.HolidayApplicationService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import reactor.core.publisher.Mono;

import java.time.LocalDate;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class HolidayServiceTest {

    @Mock
    private HolidayApiPort holidayApiClient;

    private HolidayApplicationService holidayService;

    @BeforeEach
    void setUp() {
        when(holidayApiClient.getPublicHolidays(anyInt()))
                .thenReturn(Mono.just(Set.of()));
        holidayService = new HolidayApplicationService(holidayApiClient);
    }

    @Test
    @DisplayName("Debe identificar correctamente un día festivo")
    void shouldIdentifyHoliday() {
        LocalDate christmas = LocalDate.of(LocalDate.now().getYear(), 12, 25);
        holidayService.addHoliday(christmas);

        assertTrue(holidayService.isHoliday(christmas));
    }

    @Test
    @DisplayName("Debe identificar correctamente un día no festivo")
    void shouldIdentifyNonHoliday() {
        LocalDate regularDay = LocalDate.of(LocalDate.now().getYear(), 6, 15);

        assertFalse(holidayService.isHoliday(regularDay));
    }

    @Test
    @DisplayName("Debe cargar festivos desde la API")
    void shouldLoadHolidaysFromApi() {
        Set<LocalDate> apiHolidays = Set.of(
                LocalDate.of(2024, 1, 1),
                LocalDate.of(2024, 5, 1),
                LocalDate.of(2024, 12, 25)
        );

        when(holidayApiClient.getPublicHolidays(2024))
                .thenReturn(Mono.just(apiHolidays));
        when(holidayApiClient.getPublicHolidays(2025))
                .thenReturn(Mono.just(Set.of()));

        HolidayApplicationService service = new HolidayApplicationService(holidayApiClient);
        service.refreshHolidays();

        try {
            Thread.sleep(500);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }

        Set<LocalDate> holidays = service.getHolidaysForYear(2024);
        assertFalse(holidays.isEmpty());
    }

    @Test
    @DisplayName("Debe manejar error de API y usar festivos por defecto")
    void shouldHandleApiErrorAndUseDefaults() {
        when(holidayApiClient.getPublicHolidays(anyInt()))
                .thenReturn(Mono.error(new RuntimeException("API Error")));

        HolidayApplicationService service = new HolidayApplicationService(holidayApiClient);

        Set<LocalDate> holidays = service.getHolidays();
        assertFalse(holidays.isEmpty(), "Debe tener festivos por defecto");
    }

    @Test
    @DisplayName("Debe agregar festivo manualmente")
    void shouldAddHolidayManually() {
        LocalDate customHoliday = LocalDate.of(2024, 7, 20);

        holidayService.addHoliday(customHoliday);

        assertTrue(holidayService.isHoliday(customHoliday));
    }

    @Test
    @DisplayName("Debe obtener festivos para un año específico")
    void shouldGetHolidaysForSpecificYear() {
        LocalDate holiday2024 = LocalDate.of(2024, 1, 1);
        LocalDate holiday2025 = LocalDate.of(2025, 1, 1);

        holidayService.addHoliday(holiday2024);
        holidayService.addHoliday(holiday2025);

        Set<LocalDate> holidays2024 = holidayService.getHolidaysForYear(2024);
        Set<LocalDate> holidays2025 = holidayService.getHolidaysForYear(2025);

        assertTrue(holidays2024.contains(holiday2024));
        assertFalse(holidays2024.contains(holiday2025));
        assertTrue(holidays2025.contains(holiday2025));
    }

    @Test
    @DisplayName("Debe incluir festivos fijos de Colombia")
    void shouldIncludeFixedColombianHolidays() {
        int currentYear = LocalDate.now().getYear();

        LocalDate newYear = LocalDate.of(currentYear, 1, 1);
        LocalDate laborDay = LocalDate.of(currentYear, 5, 1);
        LocalDate independence = LocalDate.of(currentYear, 7, 20);
        LocalDate christmas = LocalDate.of(currentYear, 12, 25);

        Set<LocalDate> holidays = holidayService.getHolidays();

        assertTrue(holidays.contains(newYear) || holidays.contains(newYear.plusYears(1)),
                "Debe incluir Año Nuevo");
        assertTrue(holidays.contains(laborDay) || holidays.contains(laborDay.plusYears(1)),
                "Debe incluir Día del Trabajo");
        assertTrue(holidays.contains(independence) || holidays.contains(independence.plusYears(1)),
                "Debe incluir Día de la Independencia");
        assertTrue(holidays.contains(christmas) || holidays.contains(christmas.plusYears(1)),
                "Debe incluir Navidad");
    }
}
