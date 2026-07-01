package com.medisalud.domain.model;

import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.ToString;

import java.time.LocalDateTime;

@Getter
@Builder
@ToString
@EqualsAndHashCode
public final class TimeSlot {
    private final LocalDateTime startTime;
    private final LocalDateTime endTime;
    private final boolean available;
    
    public TimeSlot(LocalDateTime startTime, LocalDateTime endTime, boolean available) {
        this.startTime = startTime;
        this.endTime = endTime;
        this.available = available;
    }
}
