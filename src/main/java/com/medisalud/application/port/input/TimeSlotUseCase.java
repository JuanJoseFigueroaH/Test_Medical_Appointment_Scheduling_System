package com.medisalud.application.port.input;

import com.medisalud.domain.model.TimeSlot;
import reactor.core.publisher.Mono;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

/**
 * Application use case for time slot operations.
 * This is the port that controllers use to interact with time slot functionality.
 */
public interface TimeSlotUseCase {
    
    List<TimeSlot> generateSlotsForDate(LocalDate date);
    
    List<TimeSlot> generateSlotsForDateRange(LocalDate startDate, LocalDate endDate);
    
    Mono<Void> validateTimeSlot(LocalDateTime dateTime);
    
    LocalDateTime normalizeToSlotStart(LocalDateTime dateTime);
}
