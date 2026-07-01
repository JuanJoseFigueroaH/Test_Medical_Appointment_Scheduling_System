package com.medisalud.infrastructure.rest.handler;

import com.medisalud.domain.exception.*;
import com.medisalud.infrastructure.rest.dto.ErrorResponse;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.bind.support.WebExchangeBindException;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import lombok.extern.slf4j.Slf4j;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {
    
    @ExceptionHandler(ResourceNotFoundException.class)
    public Mono<ResponseEntity<ErrorResponse>> handleResourceNotFoundException(
            ResourceNotFoundException ex, ServerWebExchange exchange) {
        ErrorResponse error = ErrorResponse.builder()
                .code(ex.getCode())
                .message(ex.getMessage())
                .timestamp(LocalDateTime.now())
                .path(exchange.getRequest().getPath().value())
                .build();
        return Mono.just(ResponseEntity.status(HttpStatus.NOT_FOUND).body(error));
    }
    
    @ExceptionHandler(DuplicateResourceException.class)
    public Mono<ResponseEntity<ErrorResponse>> handleDuplicateResourceException(
            DuplicateResourceException ex, ServerWebExchange exchange) {
        ErrorResponse error = ErrorResponse.builder()
                .code(ex.getCode())
                .message(ex.getMessage())
                .timestamp(LocalDateTime.now())
                .path(exchange.getRequest().getPath().value())
                .build();
        return Mono.just(ResponseEntity.status(HttpStatus.CONFLICT).body(error));
    }
    
    @ExceptionHandler(AppointmentConflictException.class)
    public Mono<ResponseEntity<ErrorResponse>> handleAppointmentConflictException(
            AppointmentConflictException ex, ServerWebExchange exchange) {
        ErrorResponse error = ErrorResponse.builder()
                .code(ex.getCode())
                .message(ex.getMessage())
                .timestamp(LocalDateTime.now())
                .path(exchange.getRequest().getPath().value())
                .build();
        return Mono.just(ResponseEntity.status(HttpStatus.CONFLICT).body(error));
    }
    
    @ExceptionHandler(PatientPenalizedException.class)
    public Mono<ResponseEntity<ErrorResponse>> handlePatientPenalizedException(
            PatientPenalizedException ex, ServerWebExchange exchange) {
        ErrorResponse error = ErrorResponse.builder()
                .code(ex.getCode())
                .message(ex.getMessage())
                .timestamp(LocalDateTime.now())
                .path(exchange.getRequest().getPath().value())
                .build();
        return Mono.just(ResponseEntity.status(HttpStatus.CONFLICT).body(error));
    }
    
    @ExceptionHandler(InvalidTimeSlotException.class)
    public Mono<ResponseEntity<ErrorResponse>> handleInvalidTimeSlotException(
            InvalidTimeSlotException ex, ServerWebExchange exchange) {
        ErrorResponse error = ErrorResponse.builder()
                .code(ex.getCode())
                .message(ex.getMessage())
                .timestamp(LocalDateTime.now())
                .path(exchange.getRequest().getPath().value())
                .build();
        return Mono.just(ResponseEntity.status(HttpStatus.BAD_REQUEST).body(error));
    }
    
    @ExceptionHandler(BusinessRuleException.class)
    public Mono<ResponseEntity<ErrorResponse>> handleBusinessRuleException(
            BusinessRuleException ex, ServerWebExchange exchange) {
        ErrorResponse error = ErrorResponse.builder()
                .code(ex.getCode())
                .message(ex.getMessage())
                .timestamp(LocalDateTime.now())
                .path(exchange.getRequest().getPath().value())
                .build();
        return Mono.just(ResponseEntity.status(HttpStatus.BAD_REQUEST).body(error));
    }
    
    @ExceptionHandler(IllegalArgumentException.class)
    public Mono<ResponseEntity<ErrorResponse>> handleIllegalArgumentException(
            IllegalArgumentException ex, ServerWebExchange exchange) {
        ErrorResponse error = ErrorResponse.builder()
                .code("VALIDATION_ERROR")
                .message(ex.getMessage())
                .timestamp(LocalDateTime.now())
                .path(exchange.getRequest().getPath().value())
                .build();
        return Mono.just(ResponseEntity.status(HttpStatus.BAD_REQUEST).body(error));
    }
    
    @ExceptionHandler(WebExchangeBindException.class)
    public Mono<ResponseEntity<ErrorResponse>> handleValidationException(
            WebExchangeBindException ex, ServerWebExchange exchange) {
        List<String> details = ex.getBindingResult().getAllErrors().stream()
                .map(error -> {
                    if (error instanceof FieldError fieldError) {
                        return fieldError.getField() + ": " + fieldError.getDefaultMessage();
                    }
                    return error.getDefaultMessage();
                })
                .collect(Collectors.toList());
        
        ErrorResponse error = ErrorResponse.builder()
                .code("VALIDATION_ERROR")
                .message("Error de validación en los datos de entrada")
                .details(details)
                .timestamp(LocalDateTime.now())
                .path(exchange.getRequest().getPath().value())
                .build();
        return Mono.just(ResponseEntity.status(HttpStatus.BAD_REQUEST).body(error));
    }
    
    @ExceptionHandler(Exception.class)
    public Mono<ResponseEntity<ErrorResponse>> handleGenericException(
            Exception ex, ServerWebExchange exchange) {
        log.error("Unhandled exception at {}: {}", exchange.getRequest().getPath().value(), ex.getMessage(), ex);
        ErrorResponse error = ErrorResponse.builder()
                .code("INTERNAL_ERROR")
                .message("Ha ocurrido un error interno. Por favor, contacte al administrador.")
                .timestamp(LocalDateTime.now())
                .path(exchange.getRequest().getPath().value())
                .build();
        return Mono.just(ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error));
    }
}
