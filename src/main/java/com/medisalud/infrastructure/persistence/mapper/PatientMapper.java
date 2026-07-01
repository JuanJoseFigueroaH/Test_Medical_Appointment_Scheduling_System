package com.medisalud.infrastructure.persistence.mapper;

import com.medisalud.domain.model.Patient;
import com.medisalud.domain.model.vo.DocumentId;
import com.medisalud.domain.model.vo.Email;
import com.medisalud.domain.model.vo.Phone;
import com.medisalud.infrastructure.persistence.entity.PatientEntity;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Named;

@Mapper(componentModel = "spring")
public interface PatientMapper {
    
    @Mapping(target = "documentId", source = "documentId", qualifiedByName = "stringToDocumentId")
    @Mapping(target = "phone", source = "phone", qualifiedByName = "stringToPhone")
    @Mapping(target = "email", source = "email", qualifiedByName = "stringToEmail")
    Patient toDomain(PatientEntity entity);
    
    @Mapping(target = "documentId", source = "documentId", qualifiedByName = "documentIdToString")
    @Mapping(target = "phone", source = "phone", qualifiedByName = "phoneToString")
    @Mapping(target = "email", source = "email", qualifiedByName = "emailToString")
    @Mapping(target = "isNew", constant = "true")
    PatientEntity toEntity(Patient domain);
    
    @Mapping(target = "documentId", source = "documentId", qualifiedByName = "documentIdToString")
    @Mapping(target = "phone", source = "phone", qualifiedByName = "phoneToString")
    @Mapping(target = "email", source = "email", qualifiedByName = "emailToString")
    @Mapping(target = "isNew", constant = "false")
    PatientEntity toEntityForUpdate(Patient domain);
    
    @Named("stringToDocumentId")
    default DocumentId stringToDocumentId(String documentId) {
        return documentId != null ? DocumentId.of(documentId) : null;
    }
    
    @Named("documentIdToString")
    default String documentIdToString(DocumentId documentId) {
        return documentId != null ? documentId.getValue() : null;
    }
    
    @Named("stringToPhone")
    default Phone stringToPhone(String phone) {
        return Phone.ofNullable(phone);
    }
    
    @Named("phoneToString")
    default String phoneToString(Phone phone) {
        return phone != null ? phone.getValue() : null;
    }
    
    @Named("stringToEmail")
    default Email stringToEmail(String email) {
        return Email.ofNullable(email);
    }
    
    @Named("emailToString")
    default String emailToString(Email email) {
        return email != null ? email.getValue() : null;
    }
}
