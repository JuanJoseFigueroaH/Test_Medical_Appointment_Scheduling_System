CREATE TABLE IF NOT EXISTS doctors (
    id UUID PRIMARY KEY,
    full_name VARCHAR(100) NOT NULL,
    specialty VARCHAR(100) NOT NULL,
    phone VARCHAR(20),
    email VARCHAR(100)
);

CREATE TABLE IF NOT EXISTS patients (
    id UUID PRIMARY KEY,
    full_name VARCHAR(100) NOT NULL,
    document_id VARCHAR(50) NOT NULL UNIQUE,
    phone VARCHAR(20) NOT NULL,
    email VARCHAR(100) NOT NULL,
    birth_date DATE,
    registration_date TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE IF NOT EXISTS appointments (
    id UUID PRIMARY KEY,
    patient_id UUID NOT NULL,
    doctor_id UUID NOT NULL,
    date_time TIMESTAMP NOT NULL,
    status VARCHAR(20) NOT NULL DEFAULT 'PROGRAMADA',
    cancellation_date_time TIMESTAMP,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    version BIGINT DEFAULT 0,
    CONSTRAINT fk_appointment_patient FOREIGN KEY (patient_id) REFERENCES patients(id) ON DELETE RESTRICT,
    CONSTRAINT fk_appointment_doctor FOREIGN KEY (doctor_id) REFERENCES doctors(id) ON DELETE RESTRICT
);

CREATE TABLE IF NOT EXISTS penalties (
    id UUID PRIMARY KEY,
    patient_id UUID NOT NULL,
    appointment_id UUID NOT NULL,
    penalty_date_time TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    reason VARCHAR(255),
    CONSTRAINT fk_penalty_patient FOREIGN KEY (patient_id) REFERENCES patients(id),
    CONSTRAINT fk_penalty_appointment FOREIGN KEY (appointment_id) REFERENCES appointments(id)
);

CREATE INDEX IF NOT EXISTS idx_appointments_doctor_datetime ON appointments(doctor_id, date_time);
CREATE INDEX IF NOT EXISTS idx_appointments_patient_datetime ON appointments(patient_id, date_time);
CREATE INDEX IF NOT EXISTS idx_appointments_status ON appointments(status);
CREATE INDEX IF NOT EXISTS idx_appointments_datetime ON appointments(date_time);
CREATE INDEX IF NOT EXISTS idx_penalties_patient_datetime ON penalties(patient_id, penalty_date_time);
CREATE INDEX IF NOT EXISTS idx_patients_document_id ON patients(document_id);
CREATE INDEX IF NOT EXISTS idx_appointments_doctor_datetime_status ON appointments(doctor_id, date_time, status);
CREATE INDEX IF NOT EXISTS idx_appointments_patient_datetime_status ON appointments(patient_id, date_time, status);

CREATE UNIQUE INDEX IF NOT EXISTS idx_unique_doctor_datetime_scheduled 
ON appointments (doctor_id, date_time) 
WHERE status = 'PROGRAMADA';

CREATE UNIQUE INDEX IF NOT EXISTS idx_unique_patient_doctor_datetime_scheduled 
ON appointments (patient_id, doctor_id, date_time) 
WHERE status = 'PROGRAMADA';

INSERT INTO doctors (id, full_name, specialty, phone, email) VALUES
    ('a1b2c3d4-e5f6-7890-abcd-ef1234567890', 'Dra. María González', 'Cardiología', '555-1001', 'maria.gonzalez@medisalud.com'),
    ('b2c3d4e5-f6a7-8901-bcde-f12345678901', 'Dr. Carlos Ruiz', 'Pediatría', '555-1002', 'carlos.ruiz@medisalud.com'),
    ('c3d4e5f6-a7b8-9012-cdef-123456789012', 'Dra. Ana López', 'Dermatología', '555-1003', 'ana.lopez@medisalud.com');
