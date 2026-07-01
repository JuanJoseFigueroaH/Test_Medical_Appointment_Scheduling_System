package com.medisalud.application.config;

import com.medisalud.domain.service.HolidayChecker;
import com.medisalud.domain.service.TimeSlotDomainService;
import com.medisalud.domain.service.impl.TimeSlotDomainServiceImpl;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.time.Clock;

@Configuration
public class ClockConfig {
    
    @Bean
    public Clock clock() {
        return Clock.systemDefaultZone();
    }
    
    @Bean
    public TimeSlotDomainService timeSlotDomainService(HolidayChecker holidayChecker) {
        return new TimeSlotDomainServiceImpl(holidayChecker);
    }
}
