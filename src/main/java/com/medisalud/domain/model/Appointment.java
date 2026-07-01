package com.medisalud.domain.model;

import com.medisalud.domain.BusinessConstants;
import lombok.Builder;
import lombok.Getter;

import java.time.Clock;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.UUID;

@Getter
@Builder(toBuilder = true)
public class Appointment {
    
    private UUID id;
    private Long version;
    private UUID patientId;
    private UUID doctorId;
    private LocalDateTime dateTime;
    private AppointmentStatus status;
    private LocalDateTime cancellationDateTime;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    
    public boolean isScheduled() {
        return AppointmentStatus.PROGRAMADA.equals(this.status);
    }
    
    public boolean isCancelled() {
        return AppointmentStatus.CANCELADA.equals(this.status);
    }
    
    public boolean isAttended() {
        return AppointmentStatus.ATENDIDA.equals(this.status);
    }
    
    public boolean isPast(Clock clock) {
        return this.dateTime != null && this.dateTime.isBefore(LocalDateTime.now(clock));
    }
    
    public boolean isFuture(Clock clock) {
        return this.dateTime != null && this.dateTime.isAfter(LocalDateTime.now(clock));
    }
    
    public long hoursUntilAppointment(Clock clock) {
        if (this.dateTime == null) return 0;
        return ChronoUnit.HOURS.between(LocalDateTime.now(clock), this.dateTime);
    }
    
    public boolean isLateCancellation(Clock clock) {
        return hoursUntilAppointment(clock) < BusinessConstants.LATE_CANCELLATION_HOURS;
    }
    
    public Appointment cancel(Clock clock) {
        if (!isScheduled()) {
            throw new IllegalStateException("Solo se pueden cancelar citas programadas");
        }
        AppointmentStatus.CANCELADA.validateTransitionFrom(this.status);
        LocalDateTime now = LocalDateTime.now(clock);
        return this.toBuilder()
                .status(AppointmentStatus.CANCELADA)
                .cancellationDateTime(now)
                .updatedAt(now)
                .build();
    }
    
    public Appointment markAsAttended(Clock clock) {
        if (!isScheduled()) {
            throw new IllegalStateException("Solo se pueden marcar como atendidas citas programadas");
        }
        AppointmentStatus.ATENDIDA.validateTransitionFrom(this.status);
        return this.toBuilder()
                .status(AppointmentStatus.ATENDIDA)
                .updatedAt(LocalDateTime.now(clock))
                .build();
    }
    
    public boolean canBeMarkedAsAttended(Clock clock) {
        if (!isScheduled()) return false;
        LocalDateTime now = LocalDateTime.now(clock);
        LocalDateTime toleranceStart = this.dateTime.minusMinutes(BusinessConstants.MARK_AS_ATTENDED_TOLERANCE_MINUTES);
        return !now.isBefore(toleranceStart);
    }
}
