package com.medisalud.domain.service;

import java.time.LocalDate;
import java.util.Set;

public interface HolidayChecker {
    boolean isHoliday(LocalDate date);
    Set<LocalDate> getHolidays();
    Set<LocalDate> getHolidaysForYear(int year);
}
