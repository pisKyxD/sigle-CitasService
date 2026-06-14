# Sigle-CitasService

Microservicio del sistema SIGLE que maneja el ciclo de vida de las citas médicas. Cuando se cancela una cita, publica un evento en RabbitMQ para que PacientesService notifique al paciente.

## Stack

- Java 17
- Spring Boot 3.2.5
- Spring Data JPA
- Spring AMQP (RabbitMQ)
- MySQL
- Lombok

## Requisitos

- Java 17+
- Maven 3.9+
- MySQL corriendo
- RabbitMQ corriendo en `localhost:5672`

## RabbitMQ local con Docker

```bash
docker run -d --name rabbitmq -p 5672:5672 -p 15672:15672 rabbitmq:management
```

Consola en `http://localhost:15672` (guest / guest)

## Configuración

```properties
server.port=8082
spring.datasource.url=jdbc:mysql://localhost:3306/sigle_citas?createDatabaseIfNotExist=true&serverTimezone=UTC
spring.datasource.username=root
spring.datasource.password=tu_password
spring.rabbitmq.host=localhost
spring.rabbitmq.port=5672
spring.rabbitmq.username=guest
spring.rabbitmq.password=guest
```

## Instalación

```bash
mvn clean package -DskipTests
java -jar target/citas-service-0.0.1-SNAPSHOT.jar
```

Disponible en `http://localhost:8082`

## Docker

```bash
docker build -t sigle-citas-service .
docker run -p 8082:10000 sigle-citas-service
```

## Endpoints

### Citas
| Método | Ruta | Descripción |
|---|---|---|
| GET | `/api/citas` | Todas las citas |
| GET | `/api/citas/{id}` | Por ID |
| GET | `/api/citas/paciente/{pacienteId}` | Citas de un paciente |
| GET | `/api/citas/medico/{medicoId}/horas-ocupadas?fecha=YYYY-MM-DD` | Horas ocupadas del médico ese día |
| POST | `/api/citas/agendar` | Agendar cita |
| PUT | `/api/citas/{id}` | Actualizar |
| POST | `/api/citas/{id}/cancelar` | Cancelar (publica evento en RabbitMQ) |
| DELETE | `/api/citas/{id}` | Eliminar |

### Médicos
| Método | Ruta | Descripción |
|---|---|---|
| GET | `/api/citas/medicos` | Todos los médicos |
| GET | `/api/citas/medicos/{id}` | Por ID |
| POST | `/api/citas/medicos` | Crear |
| PUT | `/api/citas/medicos/{id}` | Actualizar |
| DELETE | `/api/citas/medicos/{id}` | Eliminar |

## Ejemplos

### Agendar cita
```json
POST /api/citas/agendar
{
  "cita": {
    "pacienteId": 1,
    "listaEsperaId": 3,
    "especialidad": "Cardiología",
    "fechaHora": "2025-05-20T09:30:00"
  },
  "medicoId": 2
}
```

### Cancelar cita
```json
POST /api/citas/1/cancelar
{
  "motivo": "El paciente no puede asistir",
  "canceladoPor": "PACIENTE"
}
```

`canceladoPor` acepta: `PACIENTE`, `MEDICO`, `ADMINISTRACION`

## Validaciones

Los endpoints de creación/actualización validan el body con `@Valid` y anotaciones `@NotNull` / `@NotBlank` de Jakarta Validation. Peticiones con datos faltantes o inválidos devuelven `400 Bad Request` con el detalle del campo.

## RabbitMQ

Al cancelar una cita se publican en el mismo `@Transactional`:

1. La cita cambia a estado `CANCELADA`
2. Se guarda el registro de cancelación
3. Se publica el evento en RabbitMQ

| Parámetro | Valor |
|---|---|
| Exchange | `sigle.exchange` |
| Queue | `sigle.citas.canceladas` |
| Routing key | `citas.cancelada` |
| Formato | JSON |

## Tests

```bash
mvn test
```

Incluye tests unitarios (Mockito) para `CitaService` y `MedicoService`, y tests de integración (`MockMvc`) para `CitaController` y `MedicoController`, usando H2 en memoria.

### Tests con Docker

```bash
docker build -f Dockerfile.test -t citas-tests .
docker run --rm citas-tests
```

## Health

```
GET http://localhost:8082/actuator/health
```

## Estructura

```
src/main/java/com/rednorte/sigle/citas_service/
├── config/
│   └── RabbitMQConfig.java
├── controller/
├── model/
├── repository/
└── service/
```