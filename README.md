# Sigle-CitasService

Microservicio del sistema SIGLE que maneja el ciclo de vida de las citas médicas. Publica eventos en RabbitMQ cuando una cita se agenda y cuando se cancela, para que PacientesService notifique al paciente (BD + correo).

> Migrado desde Spring Boot (Java) a Node.js / Express / Sequelize.

## Stack

- Node.js 20
- Express 5
- Sequelize + MySQL2
- amqplib (RabbitMQ)
- Eureka (registro de servicio)
- Jest + Supertest (testing)
- pnpm (gestor de paquetes)

## Requisitos

- Node.js 20+
- pnpm (`npm install -g pnpm`)
- MySQL corriendo
- RabbitMQ corriendo en `localhost:5672`
- PacientesService activo si quieres ver las notificaciones que generan estos eventos

## RabbitMQ local con Docker

```bash
docker run -d --name rabbitmq -p 5672:5672 -p 15672:15672 rabbitmq:management
```

Consola en `http://localhost:15672` (guest / guest)

## Variables de entorno

Copiar `.env.example` a `.env`:

```env
PORT=10000
DB_HOST=localhost
DB_PORT=3306
DB_NAME=sigle_citas
DB_USER=root
DB_PASSWORD=tu_password
RABBITMQ_URL=amqp://guest:guest@localhost:5672

EUREKA_HOST=localhost
EUREKA_PORT=8761
INSTANCE_HOST=localhost
```

## Instalación

> Usa siempre `pnpm`, no `npm install` — el proyecto usa `pnpm-lock.yaml` como único lockfile. Si ves un `package-lock.json` en el repo, elimínalo (quedó de una instalación accidental con npm).

```bash
pnpm install
pnpm dev
```

Disponible en `http://localhost:10000` (o el puerto que definas en `PORT`).

## Docker

```bash
docker build -t sigle-citas-service .
docker run -p 10000:10000 sigle-citas-service
```

## Endpoints

### Citas
| Método | Ruta | Descripción |
|---|---|---|
| GET | `/api/citas` | Todas las citas |
| GET | `/api/citas/:id` | Por ID |
| GET | `/api/citas/paciente/:pacienteId` | Citas de un paciente |
| GET | `/api/citas/paciente/:pacienteId/paginado?page=&size=` | Citas de un paciente, paginadas |
| GET | `/api/citas/medico/:medicoId` | Citas de un médico |
| GET | `/api/citas/medico/:medicoId/horas-ocupadas?fecha=YYYY-MM-DD` | Horas ocupadas del médico ese día |
| POST | `/api/citas/agendar` | Agendar cita (publica evento `citas.creada`) |
| PUT | `/api/citas/:id` | Actualizar |
| POST | `/api/citas/:id/cancelar` | Cancelar (publica evento `citas.cancelada`) |
| DELETE | `/api/citas/:id` | Eliminar |

### Médicos
| Método | Ruta | Descripción |
|---|---|---|
| GET | `/api/citas/medicos` | Todos los médicos |
| GET | `/api/citas/medicos/:id` | Por ID |
| GET | `/api/citas/medicos/email/:email` | Por email |
| POST | `/api/citas/medicos` | Crear |
| PUT | `/api/citas/medicos/:id` | Actualizar |
| DELETE | `/api/citas/medicos/:id` | Eliminar |

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

## RabbitMQ

Este servicio es el único que publica eventos; PacientesService solo los escucha.

| Evento | Exchange | Queue | Routing key | Cuándo se publica |
|---|---|---|---|---|
| Cita creada | `sigle.exchange` | `sigle.citas.creadas` | `citas.creada` | Al agendar una cita con éxito |
| Cita cancelada | `sigle.exchange` | `sigle.citas.canceladas` | `citas.cancelada` | Al cancelar una cita |

Ambos eventos se publican en formato JSON, después de que el cambio ya quedó guardado en la base de datos (no son transaccionales con la escritura en MySQL: si RabbitMQ está caído, la cita igual se crea/cancela, solo no se genera la notificación).

**Payload de `citas.creada`:**
```json
{
  "citaId": 1,
  "pacienteId": 1,
  "listaEsperaId": 3,
  "especialidad": "Cardiología",
  "fechaHora": "2025-05-20T09:30:00",
  "medicoNombre": "Dr. Pérez"
}
```

**Payload de `citas.cancelada`:**
```json
{
  "citaId": 1,
  "pacienteId": 1,
  "listaEsperaId": 3,
  "motivo": "El paciente no puede asistir"
}
```

## Tests

```bash
pnpm test
```

Corre con Jest + Supertest. `citaService` y `medicoService` están mockeados en los tests de controllers, y el módulo `config/rabbitmq` se mockea completo (`connect`, `publishCancelacion`, `publishCreacion`) para no requerir una conexión real.

## Health

GET http://localhost:10000/actuator/health

## Estructura
src/
├── app.js                     # configuración de Express (testeable)
├── index.js                   # entry point, conecta BD, RabbitMQ y arranca servidor
├── config/
│   ├── database.js            # conexión Sequelize
│   ├── eureka.js
│   └── rabbitmq.js            # exchange, queues, publishCancelacion, publishCreacion
├── controllers/
│   ├── citaController.js
│   └── medicoController.js
├── models/
│   ├── Cita.js
│   ├── Cancelacion.js
│   └── Medico.js
├── routes/
│   ├── citas.js
│   └── medicos.js
└── services/
├── citaService.js          # agendar/cancelar publican eventos en RabbitMQ
└── medicoService.js
tests/
└── citas.test.js