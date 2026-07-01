package com.medisalud.domain.exception;

public class PatientPenalizedException extends BusinessRuleException {
    public PatientPenalizedException(int penaltyCount) {
        super("PATIENT_PENALIZED", 
              String.format("El paciente tiene %d penalizaciones en los últimos 30 días. " +
                           "No puede agendar nuevas citas hasta que transcurra el período de penalización.", 
                           penaltyCount));
    }
}
