package com.medisalud.infrastructure.persistence.mapper;

import com.medisalud.domain.model.Appointment;
import com.medisalud.domain.model.AppointmentStatus;
import com.medisalud.infrastructure.persistence.entity.AppointmentEntity;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Named;

@Mapper(componentModel = "spring")
public interface AppointmentMapper {
    
    @Mapping(target = "status", source = "status", qualifiedByName = "stringToStatus")
    Appointment toDomain(AppointmentEntity entity);
    
    @Mapping(target = "status", source = "status", qualifiedByName = "statusToString")
    @Mapping(target = "isNew", constant = "true")
    AppointmentEntity toEntity(Appointment domain);
    
    @Mapping(target = "status", source = "status", qualifiedByName = "statusToString")
    @Mapping(target = "isNew", constant = "false")
    AppointmentEntity toEntityForUpdate(Appointment domain);
    
    @Named("stringToStatus")
    default AppointmentStatus stringToStatus(String status) {
        return status != null ? AppointmentStatus.valueOf(status) : null;
    }
    
    @Named("statusToString")
    default String statusToString(AppointmentStatus status) {
        return status != null ? status.name() : null;
    }
}
