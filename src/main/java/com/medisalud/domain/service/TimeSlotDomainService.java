package com.medisalud.domain.service;

import com.medisalud.domain.model.TimeSlot;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

/**
 * Domain service for time slot generation logic.
 * Contains pure business logic without application concerns.
 */
public interface TimeSlotDomainService {
    
    List<TimeSlot> generateSlotsForDate(LocalDate date);
    
    List<TimeSlot> generateSlotsForDateRange(LocalDate startDate, LocalDate endDate);
    
    LocalDateTime normalizeToSlotStart(LocalDateTime dateTime);
}
