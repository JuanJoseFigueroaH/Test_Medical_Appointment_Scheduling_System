package com.medisalud.infrastructure.client;

import okhttp3.mockwebserver.MockResponse;
import okhttp3.mockwebserver.MockWebServer;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.test.StepVerifier;

import java.io.IOException;
import java.time.LocalDate;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("HolidayApiClient Integration Tests")
class HolidayApiClientTest {
    
    private MockWebServer mockWebServer;
    private HolidayApiClient holidayApiClient;
    
    @BeforeEach
    void setUp() throws IOException {
        mockWebServer = new MockWebServer();
        mockWebServer.start();
        
        holidayApiClient = new HolidayApiClient(WebClient.builder());
        setField(holidayApiClient, "apiUrl", mockWebServer.url("/api/v3").toString());
        setField(holidayApiClient, "countryCode", "CO");
        setField(holidayApiClient, "timeoutSeconds", 5);
    }
    
    @AfterEach
    void tearDown() throws IOException {
        mockWebServer.shutdown();
    }
    
    @Test
    @DisplayName("Should fetch holidays successfully from API")
    void shouldFetchHolidaysSuccessfully() {
        String jsonResponse = """
            [
                {"date": "2024-01-01", "localName": "Año Nuevo", "name": "New Year's Day", "countryCode": "CO", "fixed": true, "global": true, "types": ["Public"]},
                {"date": "2024-05-01", "localName": "Día del Trabajo", "name": "Labour Day", "countryCode": "CO", "fixed": true, "global": true, "types": ["Public"]}
            ]
            """;
        
        mockWebServer.enqueue(new MockResponse()
                .setBody(jsonResponse)
                .addHeader("Content-Type", "application/json"));
        
        StepVerifier.create(holidayApiClient.getPublicHolidays(2024))
                .assertNext(holidays -> {
                    assertThat(holidays).hasSize(2);
                    assertThat(holidays).contains(LocalDate.of(2024, 1, 1));
                    assertThat(holidays).contains(LocalDate.of(2024, 5, 1));
                })
                .verifyComplete();
    }
    
    @Test
    @DisplayName("Should return empty set when API returns error (graceful degradation)")
    void shouldReturnEmptySetOnApiError() {
        mockWebServer.enqueue(new MockResponse()
                .setResponseCode(500)
                .setBody("Internal Server Error"));
        
        StepVerifier.create(holidayApiClient.getPublicHolidays(2024))
                .assertNext(holidays -> {
                    assertThat(holidays).isEmpty();
                })
                .verifyComplete();
    }
    
    @Test
    @DisplayName("Should return empty set when API times out (graceful degradation)")
    void shouldReturnEmptySetOnTimeout() {
        // No response enqueued - will cause timeout
        mockWebServer.enqueue(new MockResponse()
                .setBodyDelay(10, java.util.concurrent.TimeUnit.SECONDS)
                .setBody("[]"));
        
        StepVerifier.create(holidayApiClient.getPublicHolidays(2024))
                .assertNext(holidays -> {
                    assertThat(holidays).isEmpty();
                })
                .verifyComplete();
    }
    
    @Test
    @DisplayName("Should return empty set when API returns invalid JSON")
    void shouldReturnEmptySetOnInvalidJson() {
        mockWebServer.enqueue(new MockResponse()
                .setBody("not valid json")
                .addHeader("Content-Type", "application/json"));
        
        StepVerifier.create(holidayApiClient.getPublicHolidays(2024))
                .assertNext(holidays -> {
                    assertThat(holidays).isEmpty();
                })
                .verifyComplete();
    }
    
    @Test
    @DisplayName("Should return empty set when API returns 404")
    void shouldReturnEmptySetOn404() {
        mockWebServer.enqueue(new MockResponse()
                .setResponseCode(404)
                .setBody("Not Found"));
        
        StepVerifier.create(holidayApiClient.getPublicHolidays(2024))
                .assertNext(holidays -> {
                    assertThat(holidays).isEmpty();
                })
                .verifyComplete();
    }
    
    @Test
    @DisplayName("Should handle empty holiday list from API")
    void shouldHandleEmptyHolidayList() {
        mockWebServer.enqueue(new MockResponse()
                .setBody("[]")
                .addHeader("Content-Type", "application/json"));
        
        StepVerifier.create(holidayApiClient.getPublicHolidays(2024))
                .assertNext(holidays -> {
                    assertThat(holidays).isEmpty();
                })
                .verifyComplete();
    }
    
    private void setField(Object target, String fieldName, Object value) {
        try {
            var field = target.getClass().getDeclaredField(fieldName);
            field.setAccessible(true);
            field.set(target, value);
        } catch (Exception e) {
            throw new RuntimeException("Failed to set field " + fieldName, e);
        }
    }
}
