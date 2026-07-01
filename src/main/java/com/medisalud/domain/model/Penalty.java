package com.medisalud.domain.model;

import com.medisalud.domain.BusinessConstants;
import lombok.Builder;
import lombok.Getter;

import java.time.Clock;
import java.time.LocalDateTime;
import java.util.UUID;

@Getter
@Builder(toBuilder = true)
public class Penalty {
    private final UUID id;
    private final UUID patientId;
    private final UUID appointmentId;
    private final LocalDateTime penaltyDateTime;
    private final String reason;
    
    public boolean isExpired(Clock clock) {
        if (penaltyDateTime == null) {
            return true;
        }
        LocalDateTime expirationDate = penaltyDateTime.plusDays(BusinessConstants.PENALTY_WINDOW_DAYS);
        return LocalDateTime.now(clock).isAfter(expirationDate);
    }
    
    public boolean isActive(Clock clock) {
        return !isExpired(clock);
    }
}
