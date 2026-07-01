package com.medisalud.infrastructure.rest.mapper;

import com.medisalud.domain.model.Patient;
import com.medisalud.domain.model.vo.DocumentId;
import com.medisalud.domain.model.vo.Email;
import com.medisalud.domain.model.vo.Phone;
import com.medisalud.infrastructure.rest.dto.PatientRequest;
import com.medisalud.infrastructure.rest.dto.PatientResponse;
import org.springframework.stereotype.Component;

@Component
public class PatientDtoMapper {
    
    public Patient toDomain(PatientRequest request) {
        if (request == null) return null;
        return Patient.builder()
                .fullName(request.fullName())
                .documentId(DocumentId.of(request.documentId()))
                .phone(Phone.of(request.phone()))
                .email(Email.of(request.email()))
                .birthDate(request.birthDate())
                .build();
    }
    
    public PatientResponse toResponse(Patient domain) {
        if (domain == null) return null;
        return PatientResponse.builder()
                .id(domain.getId())
                .fullName(domain.getFullName())
                .documentId(domain.getDocumentIdValue())
                .phone(domain.getPhoneValue())
                .email(domain.getEmailValue())
                .birthDate(domain.getBirthDate())
                .build();
    }
}
