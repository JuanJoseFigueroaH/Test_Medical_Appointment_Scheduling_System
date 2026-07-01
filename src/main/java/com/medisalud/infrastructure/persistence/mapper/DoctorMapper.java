package com.medisalud.infrastructure.persistence.mapper;

import com.medisalud.domain.model.Doctor;
import com.medisalud.domain.model.vo.Email;
import com.medisalud.domain.model.vo.Phone;
import com.medisalud.infrastructure.persistence.entity.DoctorEntity;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Named;

@Mapper(componentModel = "spring")
public interface DoctorMapper {
    
    @Mapping(target = "phone", source = "phone", qualifiedByName = "stringToPhone")
    @Mapping(target = "email", source = "email", qualifiedByName = "stringToEmail")
    Doctor toDomain(DoctorEntity entity);
    
    @Mapping(target = "phone", source = "phone", qualifiedByName = "phoneToString")
    @Mapping(target = "email", source = "email", qualifiedByName = "emailToString")
    @Mapping(target = "isNew", constant = "true")
    DoctorEntity toEntity(Doctor domain);
    
    @Mapping(target = "phone", source = "phone", qualifiedByName = "phoneToString")
    @Mapping(target = "email", source = "email", qualifiedByName = "emailToString")
    @Mapping(target = "isNew", constant = "false")
    DoctorEntity toEntityForUpdate(Doctor domain);
    
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
