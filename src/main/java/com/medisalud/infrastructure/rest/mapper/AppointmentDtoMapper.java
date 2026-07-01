package com.medisalud.infrastructure.rest.mapper;

import com.medisalud.domain.model.Appointment;
import com.medisalud.domain.model.TimeSlot;
import com.medisalud.infrastructure.rest.dto.AppointmentResponse;
import com.medisalud.infrastructure.rest.dto.TimeSlotResponse;
import org.springframework.stereotype.Component;

@Component
public class AppointmentDtoMapper {
    
    public AppointmentResponse toResponse(Appointment domain) {
        if (domain == null) return null;
        return AppointmentResponse.builder()
                .id(domain.getId())
                .patientId(domain.getPatientId())
                .doctorId(domain.getDoctorId())
                .dateTime(domain.getDateTime())
                .status(domain.getStatus() != null ? domain.getStatus().name() : null)
                .cancellationDateTime(domain.getCancellationDateTime())
                .createdAt(domain.getCreatedAt())
                .updatedAt(domain.getUpdatedAt())
                .build();
    }
    
    public TimeSlotResponse toTimeSlotResponse(TimeSlot slot) {
        if (slot == null) return null;
        return TimeSlotResponse.builder()
                .startTime(slot.getStartTime())
                .endTime(slot.getEndTime())
                .available(slot.isAvailable())
                .build();
    }
}
