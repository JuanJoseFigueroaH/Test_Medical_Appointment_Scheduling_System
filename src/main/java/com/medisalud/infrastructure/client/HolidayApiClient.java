package com.medisalud.infrastructure.client;

import com.medisalud.application.port.output.HolidayApiPort;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import java.time.Duration;
import java.time.LocalDate;
import java.time.Month;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Slf4j
@Component
public class HolidayApiClient implements HolidayApiPort {
    
    private final WebClient.Builder webClientBuilder;
    
    @Value("${medisalud.holidays.api.url:https://date.nager.at/api/v3}")
    private String apiUrl;
    
    @Value("${medisalud.holidays.api.country-code:CO}")
    private String countryCode;
    
    @Value("${medisalud.holidays.api.timeout-seconds:10}")
    private int timeoutSeconds;
    
    public HolidayApiClient(WebClient.Builder webClientBuilder) {
        this.webClientBuilder = webClientBuilder;
    }
    
    public Mono<Set<LocalDate>> getPublicHolidays(int year) {
        String url = String.format("%s/PublicHolidays/%d/%s", apiUrl, year, countryCode);
        
        log.info("Fetching holidays from Nager.Date API for {} year {}", countryCode, year);
        
        return webClientBuilder.build()
                .get()
                .uri(url)
                .retrieve()
                .bodyToFlux(NagerHolidayResponse.class)
                .map(response -> LocalDate.parse(response.date()))
                .collect(Collectors.toSet())
                .timeout(Duration.ofSeconds(timeoutSeconds))
                .doOnSuccess(holidays -> log.info("Fetched {} holidays for year {}", holidays.size(), year))
                .doOnError(e -> log.error("CRITICAL: Failed to fetch holidays from API for year {}. " +
                        "Appointments may be scheduled on holidays! Error: {}", year, e.getMessage()))
                .onErrorResume(e -> {
                    log.warn("HOLIDAY_API_FAILURE: Using default Colombian holidays for year {}.", year);
                    return Mono.just(getDefaultColombianHolidays(year));
                });
    }
    
    private Set<LocalDate> getDefaultColombianHolidays(int year) {
        return Set.of(
                LocalDate.of(year, Month.JANUARY, 1),
                LocalDate.of(year, Month.MAY, 1),
                LocalDate.of(year, Month.JULY, 20),
                LocalDate.of(year, Month.AUGUST, 7),
                LocalDate.of(year, Month.DECEMBER, 8),
                LocalDate.of(year, Month.DECEMBER, 25)
        );
    }
    
    record NagerHolidayResponse(
            String date,
            String localName,
            String name,
            String countryCode,
            boolean fixed,
            boolean global,
            List<String> types
    ) {}
}
