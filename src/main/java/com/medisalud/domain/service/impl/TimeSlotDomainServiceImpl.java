package com.medisalud.domain.service.impl;

import com.medisalud.domain.BusinessConstants;
import com.medisalud.domain.model.TimeSlot;
import com.medisalud.domain.service.HolidayChecker;
import com.medisalud.domain.service.TimeSlotDomainService;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;

/**
 * Pure domain implementation of TimeSlotDomainService.
 * Contains only business logic without any framework dependencies.
 */
public class TimeSlotDomainServiceImpl implements TimeSlotDomainService {
    
    private static final LocalTime WEEKDAY_START = LocalTime.of(BusinessConstants.WEEKDAY_START_HOUR, 0);
    private static final LocalTime WEEKDAY_END = LocalTime.of(BusinessConstants.WEEKDAY_END_HOUR, 0);
    private static final LocalTime SATURDAY_START = LocalTime.of(BusinessConstants.SATURDAY_START_HOUR, 0);
    private static final LocalTime SATURDAY_END = LocalTime.of(BusinessConstants.SATURDAY_END_HOUR, 0);
    
    private final HolidayChecker holidayChecker;
    
    public TimeSlotDomainServiceImpl(HolidayChecker holidayChecker) {
        this.holidayChecker = holidayChecker;
    }
    
    @Override
    public List<TimeSlot> generateSlotsForDateRange(LocalDate startDate, LocalDate endDate) {
        List<TimeSlot> slots = new ArrayList<>();
        LocalDate currentDate = startDate;
        
        while (!currentDate.isAfter(endDate)) {
            slots.addAll(generateSlotsForDate(currentDate));
            currentDate = currentDate.plusDays(1);
        }
        
        return slots;
    }
    
    @Override
    public List<TimeSlot> generateSlotsForDate(LocalDate date) {
        List<TimeSlot> slots = new ArrayList<>();
        DayOfWeek dayOfWeek = date.getDayOfWeek();
        
        if (dayOfWeek == DayOfWeek.SUNDAY || holidayChecker.isHoliday(date)) {
            return slots;
        }
        
        LocalTime startTime;
        LocalTime endTime;
        
        if (dayOfWeek == DayOfWeek.SATURDAY) {
            startTime = SATURDAY_START;
            endTime = SATURDAY_END;
        } else {
            startTime = WEEKDAY_START;
            endTime = WEEKDAY_END;
        }
        
        LocalTime currentTime = startTime;
        while (currentTime.isBefore(endTime)) {
            LocalDateTime slotStart = LocalDateTime.of(date, currentTime);
            LocalDateTime slotEnd = slotStart.plusMinutes(BusinessConstants.SLOT_DURATION_MINUTES);
            
            slots.add(TimeSlot.builder()
                    .startTime(slotStart)
                    .endTime(slotEnd)
                    .available(true)
                    .build());
            
            currentTime = currentTime.plusMinutes(BusinessConstants.SLOT_DURATION_MINUTES);
        }
        
        return slots;
    }
    
    @Override
    public LocalDateTime normalizeToSlotStart(LocalDateTime dateTime) {
        int minutes = dateTime.getMinute();
        int normalizedMinutes = (minutes / BusinessConstants.SLOT_DURATION_MINUTES) * BusinessConstants.SLOT_DURATION_MINUTES;
        return dateTime.withMinute(normalizedMinutes).withSecond(0).withNano(0);
    }
    
    public boolean isValidSlotTime(LocalDateTime dateTime) {
        DayOfWeek dayOfWeek = dateTime.getDayOfWeek();
        LocalTime time = dateTime.toLocalTime();
        LocalDate date = dateTime.toLocalDate();
        
        if (dayOfWeek == DayOfWeek.SUNDAY) {
            return false;
        }
        
        if (holidayChecker.isHoliday(date)) {
            return false;
        }
        
        if (time.getMinute() % BusinessConstants.SLOT_DURATION_MINUTES != 0) {
            return false;
        }
        
        if (dayOfWeek == DayOfWeek.SATURDAY) {
            return !time.isBefore(SATURDAY_START) && 
                   !time.isAfter(SATURDAY_END.minusMinutes(BusinessConstants.SLOT_DURATION_MINUTES));
        } else {
            return !time.isBefore(WEEKDAY_START) && 
                   !time.isAfter(WEEKDAY_END.minusMinutes(BusinessConstants.SLOT_DURATION_MINUTES));
        }
    }
    
    public String getInvalidSlotReason(LocalDateTime dateTime) {
        DayOfWeek dayOfWeek = dateTime.getDayOfWeek();
        LocalTime time = dateTime.toLocalTime();
        LocalDate date = dateTime.toLocalDate();
        
        if (dayOfWeek == DayOfWeek.SUNDAY) {
            return "No hay atención los domingos";
        }
        
        if (holidayChecker.isHoliday(date)) {
            return "No hay atención en días festivos";
        }
        
        if (time.getMinute() % BusinessConstants.SLOT_DURATION_MINUTES != 0) {
            return "Las citas solo pueden programarse en franjas de 30 minutos (ej: 08:00, 08:30, 09:00)";
        }
        
        if (dayOfWeek == DayOfWeek.SATURDAY) {
            if (time.isBefore(SATURDAY_START) || time.isAfter(SATURDAY_END.minusMinutes(BusinessConstants.SLOT_DURATION_MINUTES))) {
                return String.format("Los sábados el horario de atención es de %s a %s", SATURDAY_START, SATURDAY_END);
            }
        } else {
            if (time.isBefore(WEEKDAY_START) || time.isAfter(WEEKDAY_END.minusMinutes(BusinessConstants.SLOT_DURATION_MINUTES))) {
                return String.format("De lunes a viernes el horario de atención es de %s a %s", WEEKDAY_START, WEEKDAY_END);
            }
        }
        
        return null;
    }
}
