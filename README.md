# MediSalud - Sistema de Agendamiento de Citas Médicas

Sistema de backend (API REST) para el agendamiento de citas médicas desarrollado con **Spring Boot WebFlux**, **R2DBC** y **PostgreSQL**, siguiendo arquitectura hexagonal.

## Descripción

MediSalud es un sistema de gestión de citas médicas que permite:

- Registrar médicos y pacientes
- Agendar, cancelar y reprogramar citas médicas
- Consultar disponibilidad de horarios
- Gestionar penalizaciones por cancelaciones tardías

### Reglas de Negocio

- **Horarios de atención**: Lunes a Viernes 08:00-18:00, Sábados 08:00-13:00
- **Franjas de 30 minutos** para cada cita
- **Sin citas en domingos ni festivos**
- **Penalización** por cancelar con menos de 2 horas de anticipación
- **Bloqueo** después de 3 penalizaciones en 30 días

## Ejecución con Docker

### Prerrequisitos

- Docker y Docker Compose instalados

### Levantar el sistema

```bash
# Clonar el repositorio
git clone <repository-url>
cd Test_Medical_Appointment_Scheduling_System

# Construir y ejecutar
docker-compose up --build
```

La aplicación estará disponible en: `http://localhost:8080`

### Documentación Swagger

- **Swagger UI**: http://localhost:8080/swagger-ui.html

## API Endpoints

### Médicos

| Método | Endpoint | Descripción |
|--------|----------|-------------|
| POST | `/api/v1/doctors` | Registrar médico |
| GET | `/api/v1/doctors` | Listar médicos |
| GET | `/api/v1/doctors/{id}` | Obtener médico por ID |
| PUT | `/api/v1/doctors/{id}` | Actualizar médico |
| DELETE | `/api/v1/doctors/{id}` | Eliminar médico |

### Pacientes

| Método | Endpoint | Descripción |
|--------|----------|-------------|
| POST | `/api/v1/patients` | Registrar paciente |
| GET | `/api/v1/patients` | Listar pacientes |
| GET | `/api/v1/patients/{id}` | Obtener paciente por ID |
| PUT | `/api/v1/patients/{id}` | Actualizar paciente |
| DELETE | `/api/v1/patients/{id}` | Eliminar paciente |

### Citas

| Método | Endpoint | Descripción |
|--------|----------|-------------|
| POST | `/api/v1/appointments` | Reservar cita |
| GET | `/api/v1/appointments` | Listar citas (con filtros) |
| GET | `/api/v1/appointments/{id}` | Obtener cita por ID |
| GET | `/api/v1/appointments/available` | Consultar disponibilidad |
| POST | `/api/v1/appointments/{id}/cancel` | Cancelar cita |
| POST | `/api/v1/appointments/{id}/reschedule` | Reprogramar cita |
| POST | `/api/v1/appointments/{id}/attend` | Marcar como atendida |

### Penalizaciones

| Método | Endpoint | Descripción |
|--------|----------|-------------|
| GET | `/api/v1/patients/{patientId}/penalties` | Consultar penalizaciones |
| GET | `/api/v1/patients/{patientId}/penalties/count` | Contar penalizaciones activas |
