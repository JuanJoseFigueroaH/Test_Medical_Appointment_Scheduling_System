# Pruebas de API - Sistema de Citas Médicas MediSalud

**Base URL:** `http://localhost:8080`  
**Swagger UI:** `http://localhost:8080/swagger-ui.html`

---

## RF-01: Registro de Pacientes

### 1.1 Crear Paciente (Caso Exitoso)
```bash
curl -X POST http://localhost:8080/api/v1/patients \
  -H "Content-Type: application/json" \
  -d '{
    "fullName": "Juan Carlos Pérez García",
    "documentId": "1234567890",
    "phone": "555-1234",
    "email": "juan.perez@email.com",
    "birthDate": "1990-05-15"
  }'
```
**Respuesta esperada:** `201 Created`

### 1.2 Crear Paciente - Documento Duplicado
```bash
curl -X POST http://localhost:8080/api/v1/patients \
  -H "Content-Type: application/json" \
  -d '{
    "fullName": "Otro Paciente",
    "documentId": "1234567890",
    "phone": "555-9999",
    "email": "otro@email.com",
    "birthDate": "1985-01-01"
  }'
```
**Respuesta esperada:** `409 Conflict` - Ya existe un paciente con ese documento

### 1.3 Crear Paciente - Datos Inválidos (Email)
```bash
curl -X POST http://localhost:8080/api/v1/patients \
  -H "Content-Type: application/json" \
  -d '{
    "fullName": "Test Paciente",
    "documentId": "9999999999",
    "phone": "555-1234",
    "email": "email-invalido",
    "birthDate": "1990-05-15"
  }'
```
**Respuesta esperada:** `400 Bad Request` - Email inválido

### 1.4 Obtener Paciente por ID
```bash
curl -X GET http://localhost:8080/api/v1/patients/{patientId}
```

### 1.5 Listar Pacientes con Paginación
```bash
curl -X GET "http://localhost:8080/api/v1/patients?page=0&size=10"
```

---

## RF-02: Registro de Médicos

### 2.1 Crear Médico (Caso Exitoso)
```bash
curl -X POST http://localhost:8080/api/v1/doctors \
  -H "Content-Type: application/json" \
  -d '{
    "fullName": "Dra. María González",
    "specialty": "Cardiología",
    "email": "maria.gonzalez@medisalud.com",
    "phone": "555-0001"
  }'
```
**Respuesta esperada:** `201 Created`

### 2.2 Crear Segundo Médico
```bash
curl -X POST http://localhost:8080/api/v1/doctors \
  -H "Content-Type: application/json" \
  -d '{
    "fullName": "Dr. Carlos Rodríguez",
    "specialty": "Medicina General",
    "email": "carlos.rodriguez@medisalud.com",
    "phone": "555-0002"
  }'
```

### 2.3 Listar Médicos con Paginación
```bash
curl -X GET "http://localhost:8080/api/v1/doctors?page=0&size=10"
```

### 2.4 Obtener Médico por ID
```bash
curl -X GET http://localhost:8080/api/v1/doctors/{doctorId}
```

---

## RF-03: Agendamiento de Citas

### 3.1 Crear Cita (Caso Exitoso)
```bash
# Usar fecha futura válida (L-V 08:00-18:00, mínimo 1 hora de anticipación)
curl -X POST http://localhost:8080/api/v1/appointments \
  -H "Content-Type: application/json" \
  -d '{
    "patientId": "{patientId}",
    "doctorId": "{doctorId}",
    "dateTime": "2026-07-07T10:00:00"
  }'
```
**Respuesta esperada:** `201 Created`

### 3.2 Crear Cita - Médico No Disponible (RN-02)
```bash
# Intentar crear otra cita con el mismo médico en el mismo horario
curl -X POST http://localhost:8080/api/v1/appointments \
  -H "Content-Type: application/json" \
  -d '{
    "patientId": "{otherPatientId}",
    "doctorId": "{doctorId}",
    "dateTime": "2026-07-07T10:00:00"
  }'
```
**Respuesta esperada:** `409 Conflict` - El médico ya tiene una cita en este horario

### 3.3 Crear Cita - Paciente con Conflicto (RN-04)
```bash
# Intentar crear cita para el mismo paciente en el mismo horario con otro médico
curl -X POST http://localhost:8080/api/v1/appointments \
  -H "Content-Type: application/json" \
  -d '{
    "patientId": "{patientId}",
    "doctorId": "{otherDoctorId}",
    "dateTime": "2026-07-07T10:00:00"
  }'
```
**Respuesta esperada:** `409 Conflict` - El paciente ya tiene una cita en este horario

---

## RF-04: Consulta de Disponibilidad

### 4.1 Consultar Franjas Disponibles
```bash
curl -X GET "http://localhost:8080/api/v1/appointments/available?doctorId={doctorId}&startDate=2026-07-07&endDate=2026-07-11"
```
**Respuesta esperada:** `200 OK` - Lista de franjas horarias disponibles

### 4.2 Consultar Disponibilidad - Rango Inválido
```bash
curl -X GET "http://localhost:8080/api/v1/appointments/available?doctorId={doctorId}&startDate=2026-07-15&endDate=2026-07-10"
```
**Respuesta esperada:** `400 Bad Request` - Fecha inicio posterior a fecha fin

---

## RF-05: Cancelación de Citas

### 5.1 Cancelar Cita (Caso Exitoso)
```bash
curl -X POST http://localhost:8080/api/v1/appointments/{appointmentId}/cancel
```
**Respuesta esperada:** `200 OK` - Cita cancelada

### 5.2 Cancelar Cita - Idempotencia
```bash
# Llamar de nuevo al mismo endpoint
curl -X POST http://localhost:8080/api/v1/appointments/{appointmentId}/cancel
```
**Respuesta esperada:** `200 OK` - Retorna la misma cita cancelada (idempotente)

### 5.3 Cancelar Cita - No Existe
```bash
curl -X POST http://localhost:8080/api/v1/appointments/00000000-0000-0000-0000-000000000000/cancel
```
**Respuesta esperada:** `404 Not Found`

---

## RF-06: Listado de Citas con Filtros

### 6.1 Listar Todas las Citas
```bash
curl -X GET http://localhost:8080/api/v1/appointments
```

### 6.2 Filtrar por Médico
```bash
curl -X GET "http://localhost:8080/api/v1/appointments?doctorId={doctorId}"
```

### 6.3 Filtrar por Paciente
```bash
curl -X GET "http://localhost:8080/api/v1/appointments?patientId={patientId}"
```

### 6.4 Filtrar por Estado
```bash
curl -X GET "http://localhost:8080/api/v1/appointments?status=PROGRAMADA"
```

### 6.5 Filtrar por Rango de Fechas
```bash
curl -X GET "http://localhost:8080/api/v1/appointments?startDate=2026-07-01&endDate=2026-07-31"
```

### 6.6 Combinación de Filtros con Paginación
```bash
curl -X GET "http://localhost:8080/api/v1/appointments?doctorId={doctorId}&status=PROGRAMADA&page=0&size=10"
```

---

## Reglas de Negocio

### RN-01: Franjas Horarias de Atención

#### Horario Válido (Lunes-Viernes 08:00-18:00)
```bash
curl -X POST http://localhost:8080/api/v1/appointments \
  -H "Content-Type: application/json" \
  -d '{
    "patientId": "{patientId}",
    "doctorId": "{doctorId}",
    "dateTime": "2026-07-06T09:00:00"
  }'
```
**Respuesta esperada:** `201 Created`

#### Horario Válido (Sábado 08:00-13:00)
```bash
curl -X POST http://localhost:8080/api/v1/appointments \
  -H "Content-Type: application/json" \
  -d '{
    "patientId": "{patientId}",
    "doctorId": "{doctorId}",
    "dateTime": "2026-07-04T10:00:00"
  }'
```
**Respuesta esperada:** `201 Created`

#### Horario Inválido - Domingo
```bash
curl -X POST http://localhost:8080/api/v1/appointments \
  -H "Content-Type: application/json" \
  -d '{
    "patientId": "{patientId}",
    "doctorId": "{doctorId}",
    "dateTime": "2026-07-05T10:00:00"
  }'
```
**Respuesta esperada:** `400 Bad Request` - No hay atención los domingos

#### Horario Inválido - Fuera de Horario
```bash
curl -X POST http://localhost:8080/api/v1/appointments \
  -H "Content-Type: application/json" \
  -d '{
    "patientId": "{patientId}",
    "doctorId": "{doctorId}",
    "dateTime": "2026-07-07T19:00:00"
  }'
```
**Respuesta esperada:** `400 Bad Request` - Fuera del horario de atención

#### Horario Inválido - Sábado después de las 13:00
```bash
curl -X POST http://localhost:8080/api/v1/appointments \
  -H "Content-Type: application/json" \
  -d '{
    "patientId": "{patientId}",
    "doctorId": "{doctorId}",
    "dateTime": "2026-07-04T14:00:00"
  }'
```
**Respuesta esperada:** `400 Bad Request` - Sábados horario hasta las 13:00

---

### RN-03: Antigüedad Mínima (1 hora de anticipación)

#### Cita con Menos de 1 Hora de Anticipación
```bash
# Usar hora actual + 30 minutos
curl -X POST http://localhost:8080/api/v1/appointments \
  -H "Content-Type: application/json" \
  -d '{
    "patientId": "{patientId}",
    "doctorId": "{doctorId}",
    "dateTime": "2026-07-03T03:30:00"
  }'
```
**Respuesta esperada:** `400 Bad Request` - Mínimo 1 hora de anticipación

---

### RN-05: Penalización por Cancelación Tardía

#### Consultar Penalizaciones de un Paciente
```bash
curl -X GET "http://localhost:8080/api/v1/penalties?patientId={patientId}"
```

---

### RN-06: Reprogramación de Citas

#### Reprogramar Cita (Caso Exitoso)
```bash
curl -X PUT http://localhost:8080/api/v1/appointments/{appointmentId}/reschedule \
  -H "Content-Type: application/json" \
  -d '{
    "newDateTime": "2026-07-08T11:00:00"
  }'
```
**Respuesta esperada:** `200 OK` - Nueva cita creada

#### Reprogramar Cita - Ya Cancelada
```bash
curl -X PUT http://localhost:8080/api/v1/appointments/{cancelledAppointmentId}/reschedule \
  -H "Content-Type: application/json" \
  -d '{
    "newDateTime": "2026-07-08T11:00:00"
  }'
```
**Respuesta esperada:** `400 Bad Request` - Solo se pueden reprogramar citas PROGRAMADAS

---

## Endpoints de Monitoreo

### Health Check
```bash
curl -X GET http://localhost:8080/actuator/health
```

### Métricas
```bash
curl -X GET http://localhost:8080/actuator/metrics
```

### Prometheus Metrics
```bash
curl -X GET http://localhost:8080/actuator/prometheus
```

---

## Script de Prueba Completo (PowerShell)

```powershell
# Variables
$baseUrl = "http://localhost:8080/api/v1"

# 1. Crear Paciente
Write-Host "=== Creando Paciente ===" -ForegroundColor Green
$patient = Invoke-RestMethod -Uri "$baseUrl/patients" -Method Post -ContentType "application/json" -Body '{
    "fullName": "Juan Carlos Pérez",
    "documentId": "1234567890",
    "phone": "555-1234",
    "email": "juan@email.com",
    "birthDate": "1990-05-15"
}'
$patientId = $patient.id
Write-Host "Paciente creado: $patientId"

# 2. Crear Médico
Write-Host "`n=== Creando Médico ===" -ForegroundColor Green
$doctor = Invoke-RestMethod -Uri "$baseUrl/doctors" -Method Post -ContentType "application/json" -Body '{
    "fullName": "Dra. María González",
    "specialty": "Cardiología",
    "email": "maria@medisalud.com",
    "phone": "555-0001"
}'
$doctorId = $doctor.id
Write-Host "Médico creado: $doctorId"

# 3. Crear Cita
Write-Host "`n=== Creando Cita ===" -ForegroundColor Green
$appointmentBody = @{
    patientId = $patientId
    doctorId = $doctorId
    dateTime = "2026-07-07T10:00:00"
} | ConvertTo-Json
$appointment = Invoke-RestMethod -Uri "$baseUrl/appointments" -Method Post -ContentType "application/json" -Body $appointmentBody
$appointmentId = $appointment.id
Write-Host "Cita creada: $appointmentId"

# 4. Consultar Disponibilidad
Write-Host "`n=== Consultando Disponibilidad ===" -ForegroundColor Green
$slots = Invoke-RestMethod -Uri "$baseUrl/appointments/available-slots?doctorId=$doctorId&startDate=2026-07-07&endDate=2026-07-11"
Write-Host "Franjas disponibles: $($slots.Count)"

# 5. Listar Citas
Write-Host "`n=== Listando Citas ===" -ForegroundColor Green
$appointments = Invoke-RestMethod -Uri "$baseUrl/appointments?doctorId=$doctorId"
Write-Host "Citas encontradas: $($appointments.Count)"

# 6. Cancelar Cita
Write-Host "`n=== Cancelando Cita ===" -ForegroundColor Green
$cancelled = Invoke-RestMethod -Uri "$baseUrl/appointments/$appointmentId" -Method Delete
Write-Host "Cita cancelada: $($cancelled.status)"

# 7. Verificar Idempotencia
Write-Host "`n=== Verificando Idempotencia ===" -ForegroundColor Green
$cancelledAgain = Invoke-RestMethod -Uri "$baseUrl/appointments/$appointmentId" -Method Delete
Write-Host "Segunda cancelación (idempotente): $($cancelledAgain.status)"

Write-Host "`n=== TODAS LAS PRUEBAS COMPLETADAS ===" -ForegroundColor Cyan
```

---

## Notas Importantes

1. **Fechas:** Usar fechas futuras válidas (mínimo 1 hora de anticipación, máximo 30 días)
2. **Horarios:** L-V: 08:00-18:00, S: 08:00-13:00, D: Cerrado
3. **Franjas:** Las citas son en franjas de 30 minutos (08:00, 08:30, 09:00, etc.)
4. **Paginación:** Máximo 100 elementos por página
5. **Idempotencia:** El endpoint de cancelación es idempotente
