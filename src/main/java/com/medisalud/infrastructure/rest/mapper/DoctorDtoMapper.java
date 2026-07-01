package com.medisalud.infrastructure.rest.mapper;

import com.medisalud.domain.model.Doctor;
import com.medisalud.infrastructure.rest.dto.DoctorRequest;
import com.medisalud.infrastructure.rest.dto.DoctorResponse;
import org.springframework.stereotype.Component;

@Component
public class DoctorDtoMapper {
    
    public Doctor toDomain(DoctorRequest request) {
        if (request == null) return null;
        return Doctor.builder()
                .fullName(request.fullName())
                .specialty(request.specialty())
                .phone(com.medisalud.domain.model.vo.Phone.ofNullable(request.phone()))
                .email(com.medisalud.domain.model.vo.Email.ofNullable(request.email()))
                .build();
    }
    
    public DoctorResponse toResponse(Doctor domain) {
        if (domain == null) return null;
        return DoctorResponse.builder()
                .id(domain.getId())
                .fullName(domain.getFullName())
                .specialty(domain.getSpecialty())
                .phone(domain.getPhoneValue())
                .email(domain.getEmailValue())
                .build();
    }
}
