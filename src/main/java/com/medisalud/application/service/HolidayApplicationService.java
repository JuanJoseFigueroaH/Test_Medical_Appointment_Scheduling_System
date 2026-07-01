package com.medisalud.application.service;

import com.medisalud.application.config.HolidayProperties;
import com.medisalud.application.port.output.HolidayApiPort;
import com.medisalud.domain.service.HolidayChecker;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;

import java.time.LocalDate;
import java.time.MonthDay;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class HolidayApplicationService implements HolidayChecker {
    
    private final HolidayApiPort holidayApiClient;
    private final HolidayProperties holidayProperties;
    
    private final Set<LocalDate> holidays = ConcurrentHashMap.newKeySet();
    
    @PostConstruct
    public void init() {
        loadHolidaysAsync();
    }
    
    @Scheduled(cron = "${medisalud.holidays.api.refresh-cron:0 0 0 1 1 *}")
    public void scheduledRefresh() {
        log.info("Scheduled holiday refresh triggered");
        loadHolidaysAsync();
    }
    
    private void loadHolidaysAsync() {
        int currentYear = LocalDate.now().getYear();
        int nextYear = currentYear + 1;
        
        loadFromConfiguration(currentYear, nextYear);
        
        if (holidays.isEmpty()) {
            loadDefaultHolidays(currentYear, nextYear);
        }
        
        if (holidayProperties.getApi().isEnabled()) {
            loadFromApiAsync(currentYear, nextYear);
        }
        
        log.info("Initial holidays loaded: {}", holidays.size());
    }
    
    private void loadFromApiAsync(int currentYear, int nextYear) {
        Mono.zip(
                holidayApiClient.getPublicHolidays(currentYear),
                holidayApiClient.getPublicHolidays(nextYear)
        )
        .subscribeOn(Schedulers.boundedElastic())
        .subscribe(
                tuple -> {
                    Set<LocalDate> currentYearHolidays = tuple.getT1();
                    Set<LocalDate> nextYearHolidays = tuple.getT2();
                    
                    if (!currentYearHolidays.isEmpty()) {
                        holidays.addAll(currentYearHolidays);
                        log.info("Loaded {} holidays from API for year {}", currentYearHolidays.size(), currentYear);
                    }
                    if (!nextYearHolidays.isEmpty()) {
                        holidays.addAll(nextYearHolidays);
                        log.info("Loaded {} holidays from API for year {}", nextYearHolidays.size(), nextYear);
                    }
                    log.info("Total holidays after API load: {}", holidays.size());
                },
                error -> log.warn("Failed to load holidays from API: {}", error.getMessage())
        );
    }
    
    private void loadFromConfiguration(int currentYear, int nextYear) {
        for (String fixedHoliday : holidayProperties.getFixed()) {
            try {
                MonthDay monthDay = MonthDay.parse("--" + fixedHoliday);
                holidays.add(monthDay.atYear(currentYear));
                holidays.add(monthDay.atYear(nextYear));
            } catch (Exception ex) {
                log.warn("Invalid fixed holiday format: {}. Expected MM-DD", fixedHoliday);
            }
        }
        
        for (String specificDate : holidayProperties.getSpecific()) {
            try {
                holidays.add(LocalDate.parse(specificDate));
            } catch (Exception ex) {
                log.warn("Invalid specific holiday format: {}. Expected YYYY-MM-DD", specificDate);
            }
        }
        
        if (!holidayProperties.getFixed().isEmpty() || !holidayProperties.getSpecific().isEmpty()) {
            log.info("Loaded {} additional holidays from configuration", 
                    holidayProperties.getFixed().size() + holidayProperties.getSpecific().size());
        }
    }
    
    private void loadDefaultHolidays(int currentYear, int nextYear) {
        log.info("No holidays from API or config, loading defaults for Colombia");
        addDefaultHolidaysForYear(currentYear);
        addDefaultHolidaysForYear(nextYear);
    }
    
    private void addDefaultHolidaysForYear(int year) {
        holidays.add(LocalDate.of(year, 1, 1));
        holidays.add(LocalDate.of(year, 5, 1));
        holidays.add(LocalDate.of(year, 7, 20));
        holidays.add(LocalDate.of(year, 8, 7));
        holidays.add(LocalDate.of(year, 12, 8));
        holidays.add(LocalDate.of(year, 12, 25));
    }
    
    @Override
    public boolean isHoliday(LocalDate date) {
        return holidays.contains(date);
    }
    
    @Override
    public Set<LocalDate> getHolidays() {
        return new HashSet<>(holidays);
    }
    
    @Override
    public Set<LocalDate> getHolidaysForYear(int year) {
        return holidays.stream()
                .filter(d -> d.getYear() == year)
                .collect(Collectors.toSet());
    }
    
    public void refreshHolidays() {
        log.info("Manual holiday refresh triggered");
        loadHolidaysAsync();
    }
}
