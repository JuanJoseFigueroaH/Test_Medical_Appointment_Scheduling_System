package com.medisalud.infrastructure.config;

import com.medisalud.application.port.output.HolidayApiPort;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.actuate.health.Health;
import org.springframework.boot.actuate.health.ReactiveHealthIndicator;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

import java.time.Duration;
import java.time.LocalDate;

@Component
@RequiredArgsConstructor
public class HolidayApiHealthIndicator implements ReactiveHealthIndicator {

    private final HolidayApiPort holidayApiPort;

    @Override
    public Mono<Health> health() {
        return holidayApiPort.getPublicHolidays(LocalDate.now().getYear())
                .timeout(Duration.ofSeconds(5))
                .map(holidays -> Health.up()
                        .withDetail("service", "Nager.Date Holiday API")
                        .withDetail("holidaysLoaded", holidays.size())
                        .build())
                .onErrorResume(ex -> Mono.just(Health.down()
                        .withDetail("service", "Nager.Date Holiday API")
                        .withDetail("error", ex.getMessage())
                        .build()));
    }
}
