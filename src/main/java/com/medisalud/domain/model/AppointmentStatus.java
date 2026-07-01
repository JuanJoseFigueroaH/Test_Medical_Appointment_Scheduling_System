package com.medisalud.domain.model;

import java.util.EnumSet;
import java.util.Set;

public enum AppointmentStatus {
    PROGRAMADA {
        @Override
        public Set<AppointmentStatus> getValidTransitionsFrom() {
            return EnumSet.noneOf(AppointmentStatus.class);
        }
    },
    CANCELADA {
        @Override
        public Set<AppointmentStatus> getValidTransitionsFrom() {
            return EnumSet.of(PROGRAMADA);
        }
    },
    ATENDIDA {
        @Override
        public Set<AppointmentStatus> getValidTransitionsFrom() {
            return EnumSet.of(PROGRAMADA);
        }
    };
    
    public abstract Set<AppointmentStatus> getValidTransitionsFrom();
    
    public boolean canTransitionFrom(AppointmentStatus from) {
        if (from == null) {
            return this == PROGRAMADA;
        }
        return getValidTransitionsFrom().contains(from);
    }
    
    public AppointmentStatus validateTransitionFrom(AppointmentStatus from) {
        if (!canTransitionFrom(from)) {
            throw new IllegalStateException(
                    String.format("Transición de estado inválida: %s -> %s", from, this));
        }
        return this;
    }
}
