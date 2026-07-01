package com.medisalud.infrastructure.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import io.swagger.v3.oas.models.servers.Server;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;

@Configuration
public class OpenApiConfig {
    
    @Bean
    public OpenAPI customOpenAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("MediSalud - Sistema de Agendamiento de Citas Médicas")
                        .version("1.0.0")
                        .description("API REST para el sistema de agendamiento de citas médicas de MediSalud. " +
                                "Permite gestionar médicos, pacientes y citas médicas con validación de " +
                                "horarios, control de duplicidad y sistema de penalizaciones.")
                        .contact(new Contact()
                                .name("MediSalud IT")
                                .email("it@medisalud.com"))
                        .license(new License()
                                .name("Propietario")
                                .url("https://medisalud.com")))
                .servers(List.of(
                        new Server().url("http://localhost:8080").description("Servidor de desarrollo")
                ));
    }
}
