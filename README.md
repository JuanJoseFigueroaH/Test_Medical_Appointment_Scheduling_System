# MediSalud - Sistema de Agendamiento de Citas Médicas

Sistema de backend (API REST) para el agendamiento de citas médicas desarrollado con Spring Boot WebFlux y arquitectura hexagonal.

## Tecnologías Utilizadas

- **Java 17**
- **Spring Boot 3.2.0**
- **Spring WebFlux** (Programación reactiva)
- **R2DBC** (Acceso reactivo a base de datos)
- **PostgreSQL** (Base de datos)
- **Flyway** (Migraciones de base de datos)
- **SpringDoc OpenAPI** (Documentación Swagger)
- **Docker & Docker Compose**
- **JUnit 5 & Reactor Test** (Pruebas unitarias)
- **Lombok**

## Arquitectura

El proyecto sigue una **Arquitectura Hexagonal** (Ports and Adapters) con los siguientes principios:

- **SOLID**: Principios de diseño orientado a objetos
- **DRY**: Don't Repeat Yourself
- **KISS**: Keep It Simple, Stupid
- **YAGNI**: You Aren't Gonna Need It
- **CQRS**: Separación de comandos y consultas (simplificado)

### Estructura del Proyecto

```
src/main/java/com/medisalud/
├── domain/                          # Capa de Dominio
│   ├── model/                       # Entidades de dominio
│   ├── exception/                   # Excepciones de dominio
│   └── service/                     # Servicios de dominio
├── application/                     # Capa de Aplicación
│   ├── port/
│   │   ├── input/                   # Puertos de entrada (Use Cases)
│   │   └── output/                  # Puertos de salida (Repositories)
│   └── service/                     # Implementación de casos de uso
└── infrastructure/                  # Capa de Infraestructura
    ├── config/                      # Configuraciones
    ├── persistence/                 # Adaptadores de persistencia
    │   ├── adapter/                 # Implementación de repositorios
    │   ├── entity/                  # Entidades JPA/R2DBC
    │   ├── mapper/                  # Mappers entidad-dominio
    │   └── repository/              # Interfaces R2DBC
    └── rest/                        # Adaptadores REST
        ├── controller/              # Controladores
        ├── dto/                     # DTOs de request/response
        ├── handler/                 # Manejadores de excepciones
        └── mapper/                  # Mappers DTO-dominio
```

## Requerimientos Funcionales Implementados

| Código | Descripción | Estado |
|--------|-------------|--------|
| RF-01 | Registro de Médicos | ✅ |
| RF-02 | Registro de Pacientes | ✅ |
| RF-03 | Reserva de Citas | ✅ |
| RF-04 | Consulta de Citas Disponibles | ✅ |
| RF-05 | Cancelación de Citas | ✅ |
| RF-06 | Listado de Citas con Filtros | ✅ |

## Reglas de Negocio Implementadas

| Código | Descripción | Estado |
|--------|-------------|--------|
| RN-01 | Franjas Horarias de Atención (L-V: 08:00-18:00, S: 08:00-13:00) | ✅ |
| RN-02 | No Duplicidad de Citas (médico) | ✅ |
| RN-03 | Antigüedad Mínima del Paciente | ✅ |
| RN-04 | Conflicto de Paciente (cualquier médico, mismo horario) | ✅ |
| RN-05 | Penalización por Cancelación Tardía (<2 horas) | ✅ |
| RN-06 | Reprogramación de Citas | ✅ |

### Nota sobre RN-04 (Conflicto de Paciente)

> **Implementación extendida**: Aunque el enunciado especifica que "un paciente no puede tener dos citas con el **mismo médico** en la misma franja horaria", la implementación actual aplica una restricción más estricta por razones de negocio reales.
> 
> La implementación **no permite** que un paciente tenga citas con **ningún médico** al mismo tiempo, ya que físicamente un paciente no puede estar en dos consultas simultáneas.
> 
> Esta restricción se implementa en `AppointmentValidator.checkPatientHasNoOtherAppointmentAtSameTime()`.

## Ejecución

### Prerrequisitos

- Java 17+
- Maven 3.8+
- Docker y Docker Compose

### Opción 1: Docker Compose (Recomendado)

```bash
# Construir y ejecutar todos los servicios
docker-compose up --build

# Solo la base de datos (para desarrollo local)
docker-compose up postgres
```

### Opción 2: Ejecución Local

1. Iniciar PostgreSQL:
```bash
docker-compose up postgres
```

2. Ejecutar la aplicación:
```bash
mvn spring-boot:run
```

O compilar y ejecutar el JAR:
```bash
mvn clean package -DskipTests
java -jar target/medical-appointment-system-1.0.0-SNAPSHOT.jar
```

## API Endpoints

### Médicos (`/api/v1/doctors`)

| Método | Endpoint | Descripción |
|--------|----------|-------------|
| POST | `/api/v1/doctors` | Registrar médico |
| GET | `/api/v1/doctors` | Listar médicos |
| GET | `/api/v1/doctors/{id}` | Obtener médico por ID |
| PUT | `/api/v1/doctors/{id}` | Actualizar médico |
| DELETE | `/api/v1/doctors/{id}` | Eliminar médico |

### Pacientes (`/api/v1/patients`)

| Método | Endpoint | Descripción |
|--------|----------|-------------|
| POST | `/api/v1/patients` | Registrar paciente |
| GET | `/api/v1/patients` | Listar pacientes |
| GET | `/api/v1/patients/{id}` | Obtener paciente por ID |
| GET | `/api/v1/patients/document/{documentId}` | Obtener paciente por documento |
| PUT | `/api/v1/patients/{id}` | Actualizar paciente |
| DELETE | `/api/v1/patients/{id}` | Eliminar paciente |

### Citas (`/api/v1/appointments`)

| Método | Endpoint | Descripción |
|--------|----------|-------------|
| POST | `/api/v1/appointments` | Reservar cita |
| GET | `/api/v1/appointments` | Listar citas (con filtros) |
| GET | `/api/v1/appointments/{id}` | Obtener cita por ID |
| GET | `/api/v1/appointments/available` | Consultar disponibilidad |
| POST | `/api/v1/appointments/{id}/cancel` | Cancelar cita |
| POST | `/api/v1/appointments/{id}/reschedule` | Reprogramar cita |
| POST | `/api/v1/appointments/{id}/attend` | Marcar como atendida |

### Penalizaciones (`/api/v1/patients/{patientId}/penalties`)

| Método | Endpoint | Descripción |
|--------|----------|-------------|
| GET | `/api/v1/patients/{patientId}/penalties` | Consultar penalizaciones del paciente |
| GET | `/api/v1/patients/{patientId}/penalties/count` | Contar penalizaciones activas |

## Documentación API (Swagger)

Una vez ejecutada la aplicación, acceder a:

- **Swagger UI**: http://localhost:8080/swagger-ui.html
- **OpenAPI JSON**: http://localhost:8080/api-docs

## Pruebas

Ejecutar pruebas unitarias:

```bash
mvn test
```

Ejecutar pruebas con reporte de cobertura:

```bash
mvn test jacoco:report
```

## Datos Iniciales

El sistema incluye datos de ejemplo para médicos:

| ID | Nombre | Especialidad | Teléfono | Email |
|----|--------|--------------|----------|-------|
| 1 | Dra. María González | Cardiología | 555-1001 | maria.gonzalez@medisalud.com |
| 2 | Dr. Carlos Ruiz | Pediatría | 555-1002 | carlos.ruiz@medisalud.com |
| 3 | Dra. Ana López | Dermatología | 555-1003 | ana.lopez@medisalud.com |

## Códigos de Error HTTP

| Código | Descripción |
|--------|-------------|
| 200 | Operación exitosa |
| 201 | Recurso creado exitosamente |
| 400 | Error de validación o regla de negocio |
| 404 | Recurso no encontrado |
| 409 | Conflicto (duplicidad, horario ocupado, paciente penalizado) |
| 500 | Error interno del servidor |

## Ejemplos de Uso

### Crear un paciente

```bash
curl -X POST http://localhost:8080/api/v1/patients \
  -H "Content-Type: application/json" \
  -d '{
    "fullName": "Juan Pérez García",
    "documentId": "12345678",
    "phone": "5552001",
    "email": "juan.perez@email.com",
    "birthDate": "1990-05-15"
  }'
```

### Reservar una cita

```bash
curl -X POST http://localhost:8080/api/v1/appointments \
  -H "Content-Type: application/json" \
  -d '{
    "patientId": "UUID_DEL_PACIENTE",
    "doctorId": "a1b2c3d4-e5f6-7890-abcd-ef1234567890",
    "dateTime": "2024-12-20T10:00:00"
  }'
```

### Consultar disponibilidad

```bash
curl "http://localhost:8080/api/v1/appointments/available?doctorId=a1b2c3d4-e5f6-7890-abcd-ef1234567890&startDate=2024-12-20&endDate=2024-12-20"
```

### Cancelar una cita

```bash
curl -X POST http://localhost:8080/api/v1/appointments/UUID_DE_LA_CITA/cancel
```

## Seguridad

> ⚠️ **Nota MVP**: Este sistema es un MVP (Producto Mínimo Viable) y **no incluye autenticación ni autorización**. 
> En un entorno de producción, se debe implementar:
> - Autenticación JWT o OAuth2
> - Autorización basada en roles (RBAC)
> - HTTPS obligatorio
> - Auditoría de accesos

### Características de Seguridad Implementadas

- **Rate Limiting**: Límite de 60 peticiones/minuto por IP y 200/minuto por endpoint
- **CORS**: Configuración restrictiva por defecto
- **Security Headers**: X-Content-Type-Options, X-Frame-Options, X-XSS-Protection
- **Validación de entrada**: Validaciones en DTOs con Jakarta Validation
- **Manejo seguro de errores**: No se exponen detalles internos en errores 500

## Observabilidad

### Endpoints de Monitoreo

| Endpoint | Descripción |
|----------|-------------|
| `/actuator/health` | Estado de salud de la aplicación |
| `/actuator/metrics` | Métricas de la aplicación |
| `/actuator/prometheus` | Métricas en formato Prometheus |

### Métricas de Negocio

- `medisalud.appointments.created` - Total de citas creadas
- `medisalud.appointments.cancelled` - Total de citas canceladas
- `medisalud.appointments.rescheduled` - Total de citas reprogramadas
- `medisalud.penalties.applied` - Total de penalizaciones aplicadas
- `medisalud.appointments.late_cancellations` - Cancelaciones tardías

### Configuración Recomendada para Alertas (Prometheus/Grafana)

```yaml
# Ejemplo de reglas de alerta
groups:
  - name: medisalud
    rules:
      - alert: HighLateCancellationRate
        expr: rate(medisalud_appointments_late_cancellations_total[1h]) > 10
        for: 5m
        labels:
          severity: warning
      - alert: HighErrorRate
        expr: rate(http_server_requests_seconds_count{status=~"5.."}[5m]) > 0.1
        for: 2m
        labels:
          severity: critical
```

## Variables de Entorno

| Variable | Descripción | Requerida |
|----------|-------------|-----------|
| `DB_PASSWORD` | Contraseña de la base de datos | ✅ Sí |
| `DB_USERNAME` | Usuario de la base de datos | No (default: medisalud) |
| `DB_R2DBC_URL` | URL R2DBC de conexión | No (default: localhost) |
| `DB_JDBC_URL` | URL JDBC para Flyway | No (default: localhost) |

## Autor

Prueba Técnica - Sistema de Agendamiento de Citas Médicas MediSalud
