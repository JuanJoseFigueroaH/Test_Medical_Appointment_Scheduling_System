package com.medisalud.application.port.output;

import reactor.core.publisher.Mono;

import java.time.LocalDate;
import java.util.Set;

public interface HolidayApiPort {
    Mono<Set<LocalDate>> getPublicHolidays(int year);
}
