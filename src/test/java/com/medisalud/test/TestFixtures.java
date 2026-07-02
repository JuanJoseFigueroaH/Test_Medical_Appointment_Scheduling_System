package com.medisalud.test;

import com.medisalud.application.port.output.HolidayApiPort;
import com.medisalud.application.service.HolidayApplicationService;
import com.medisalud.domain.service.HolidayChecker;
import com.medisalud.application.config.HolidayProperties;
import io.micrometer.core.instrument.Counter;
import org.springframework.transaction.reactive.TransactionalOperator;
import reactor.core.publisher.Mono;

import java.util.Collections;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public final class TestFixtures {
    
    private TestFixtures() {
    }
    
    public static HolidayApiPort createMockHolidayApiPort() {
        HolidayApiPort mockClient = mock(HolidayApiPort.class);
        when(mockClient.getPublicHolidays(anyInt()))
                .thenReturn(Mono.just(Collections.emptySet()));
        return mockClient;
    }
    
    public static HolidayProperties createDefaultHolidayProperties() {
        return new HolidayProperties();
    }
    
    public static HolidayChecker createMockHolidayChecker() {
        return new HolidayApplicationService(createMockHolidayApiPort(), createDefaultHolidayProperties());
    }
    
    @SuppressWarnings("unchecked")
    public static TransactionalOperator createMockTransactionalOperator() {
        TransactionalOperator mockOperator = mock(TransactionalOperator.class);
        when(mockOperator.transactional(any(Mono.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));
        return mockOperator;
    }
    
    public static Counter createMockCounter() {
        return mock(Counter.class);
    }
}
