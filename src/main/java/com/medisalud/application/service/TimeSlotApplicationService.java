package com.medisalud.application.service;

import com.medisalud.application.port.input.TimeSlotUseCase;
import com.medisalud.domain.BusinessConstants;
import com.medisalud.domain.exception.InvalidTimeSlotException;
import com.medisalud.domain.model.TimeSlot;
import com.medisalud.domain.service.HolidayChecker;
import com.medisalud.domain.service.TimeSlotDomainService;
import com.medisalud.domain.service.impl.TimeSlotDomainServiceImpl;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

import java.time.Clock;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

/**
 * Application service that delegates domain logic to TimeSlotDomainServiceImpl
 * and adds application-specific concerns (reactive wrappers, Clock-based validation).
 */
@Slf4j
@Service
public class TimeSlotApplicationService implements TimeSlotUseCase {
    
    private final TimeSlotDomainService domainService;
    private final TimeSlotDomainServiceImpl domainServiceImpl;
    private final Clock clock;
    
    @Autowired
    public TimeSlotApplicationService(TimeSlotDomainService domainService, Clock clock) {
        this.domainService = domainService;
        this.domainServiceImpl = domainService instanceof TimeSlotDomainServiceImpl 
                ? (TimeSlotDomainServiceImpl) domainService 
                : null;
        this.clock = clock;
    }
    
    public TimeSlotApplicationService(HolidayChecker holidayChecker, Clock clock) {
        TimeSlotDomainServiceImpl impl = new TimeSlotDomainServiceImpl(holidayChecker);
        this.domainService = impl;
        this.domainServiceImpl = impl;
        this.clock = clock;
    }
    
    @Override
    public List<TimeSlot> generateSlotsForDateRange(LocalDate startDate, LocalDate endDate) {
        return domainService.generateSlotsForDateRange(startDate, endDate);
    }
    
    @Override
    public List<TimeSlot> generateSlotsForDate(LocalDate date) {
        return domainService.generateSlotsForDate(date);
    }
    
    @Override
    public Mono<Void> validateTimeSlot(LocalDateTime dateTime) {
        return Mono.defer(() -> {
            if (domainServiceImpl != null) {
                String invalidReason = domainServiceImpl.getInvalidSlotReason(dateTime);
                if (invalidReason != null) {
                    return Mono.error(new InvalidTimeSlotException(invalidReason));
                }
            }
            
            LocalDateTime now = LocalDateTime.now(clock);
            if (dateTime.isBefore(now)) {
                return Mono.error(new InvalidTimeSlotException("No se pueden programar citas en fechas pasadas"));
            }
            
            LocalDateTime minAllowedTime = now.plusHours(BusinessConstants.MIN_ADVANCE_HOURS);
            if (dateTime.isBefore(minAllowedTime)) {
                return Mono.error(new InvalidTimeSlotException(
                        String.format("Las citas deben programarse con al menos %d hora(s) de antelación", 
                                BusinessConstants.MIN_ADVANCE_HOURS)));
            }
            
            LocalDateTime maxDate = now.plusDays(BusinessConstants.MAX_DAYS_IN_ADVANCE);
            if (dateTime.isAfter(maxDate)) {
                return Mono.error(new InvalidTimeSlotException(
                        String.format("No se pueden programar citas con más de %d días de anticipación", BusinessConstants.MAX_DAYS_IN_ADVANCE)));
            }
            
            return Mono.empty();
        });
    }
    
    @Override
    public LocalDateTime normalizeToSlotStart(LocalDateTime dateTime) {
        LocalDateTime normalized = domainService.normalizeToSlotStart(dateTime);
        
        if (!normalized.equals(dateTime.withSecond(0).withNano(0))) {
            log.warn("Hora de cita normalizada de {} a {} (franja de {} minutos)", 
                    dateTime, normalized, BusinessConstants.SLOT_DURATION_MINUTES);
        }
        
        return normalized;
    }
}
