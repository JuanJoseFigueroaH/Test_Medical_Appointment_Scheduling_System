package com.medisalud.infrastructure.persistence.mapper;

import com.medisalud.domain.model.Penalty;
import com.medisalud.infrastructure.persistence.entity.PenaltyEntity;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface PenaltyMapper {
    
    Penalty toDomain(PenaltyEntity entity);
    
    PenaltyEntity toEntity(Penalty domain);
}
