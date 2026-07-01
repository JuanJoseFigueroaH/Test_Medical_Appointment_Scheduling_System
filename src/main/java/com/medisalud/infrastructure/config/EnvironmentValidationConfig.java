package com.medisalud.infrastructure.config;

import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;

@Slf4j
@Configuration
public class EnvironmentValidationConfig {
    
    @Value("${spring.r2dbc.password:}")
    private String dbPassword;
    
    @Value("${spring.r2dbc.url:}")
    private String dbUrl;
    
    @PostConstruct
    public void validateRequiredEnvironmentVariables() {
        log.info("Validating required environment variables...");
        
        if (dbPassword == null || dbPassword.isBlank()) {
            throw new IllegalStateException(
                    "FATAL: DB_PASSWORD environment variable is required but not set. " +
                    "Please set DB_PASSWORD before starting the application.");
        }
        
        if (dbUrl == null || dbUrl.isBlank()) {
            throw new IllegalStateException(
                    "FATAL: Database URL is required but not configured. " +
                    "Please set DB_R2DBC_URL or configure spring.r2dbc.url.");
        }
        
        log.info("Environment validation completed successfully");
    }
}
