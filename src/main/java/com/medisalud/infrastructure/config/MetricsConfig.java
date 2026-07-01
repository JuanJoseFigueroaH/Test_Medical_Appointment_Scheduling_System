package com.medisalud.infrastructure.config;

import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.MeterRegistry;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class MetricsConfig {

    @Bean
    public Counter appointmentsCreatedCounter(MeterRegistry registry) {
        return Counter.builder("medisalud.appointments.created")
                .description("Total de citas creadas")
                .tag("type", "created")
                .register(registry);
    }

    @Bean
    public Counter appointmentsCancelledCounter(MeterRegistry registry) {
        return Counter.builder("medisalud.appointments.cancelled")
                .description("Total de citas canceladas")
                .tag("type", "cancelled")
                .register(registry);
    }

    @Bean
    public Counter appointmentsRescheduledCounter(MeterRegistry registry) {
        return Counter.builder("medisalud.appointments.rescheduled")
                .description("Total de citas reprogramadas")
                .tag("type", "rescheduled")
                .register(registry);
    }

    @Bean
    public Counter penaltiesAppliedCounter(MeterRegistry registry) {
        return Counter.builder("medisalud.penalties.applied")
                .description("Total de penalizaciones aplicadas")
                .tag("type", "penalty")
                .register(registry);
    }

    @Bean
    public Counter lateCancellationsCounter(MeterRegistry registry) {
        return Counter.builder("medisalud.appointments.late_cancellations")
                .description("Total de cancelaciones tardías (menos de 2 horas)")
                .tag("type", "late_cancellation")
                .register(registry);
    }
}
