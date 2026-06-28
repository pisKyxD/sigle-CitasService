const request = require('supertest');

jest.mock('../src/services/citaService');
jest.mock('../src/services/medicoService');
jest.mock('../src/config/rabbitmq', () => ({ connect: jest.fn(), publishCancelacion: jest.fn() }));

const citaService = require('../src/services/citaService');
const medicoService = require('../src/services/medicoService');
const app = require('../src/app');

const mockMedico = { id: 1, rut: '12345678-9', nombre: 'Dr. Pérez', especialidad: 'Cardiología', establecimientoId: 1, activo: true };
const mockCita = { id: 1, pacienteId: 100, medicoId: 1, especialidad: 'Cardiología', fechaHora: '2025-06-25T09:30:00', estado: 'PROGRAMADA', medico: mockMedico };
const mockCancelacion = { id: 1, citaId: 1, motivo: 'Paciente no puede asistir', canceladoPor: 'PACIENTE', fechaCancelacion: new Date(), reasignado: false };
const mockPaginado = { content: [mockCita], totalElements: 1, totalPages: 1, currentPage: 0 };

// ===================== HEALTH =====================
describe('GET /actuator/health', () => {
  it('retorna status UP', async () => {
    const res = await request(app).get('/actuator/health');
    expect(res.status).toBe(200);
    expect(res.body.status).toBe('UP');
  });
});

// ===================== CITAS =====================
describe('GET /api/citas', () => {
  it('retorna lista de citas con 200', async () => {
    citaService.getAll.mockResolvedValue([mockCita]);
    const res = await request(app).get('/api/citas');
    expect(res.status).toBe(200);
    expect(res.body).toHaveLength(1);
    expect(res.body[0].especialidad).toBe('Cardiología');
  });
});

describe('GET /api/citas/:id', () => {
  it('retorna cita por ID', async () => {
    citaService.getById.mockResolvedValue(mockCita);
    const res = await request(app).get('/api/citas/1');
    expect(res.status).toBe(200);
    expect(res.body.id).toBe(1);
  });

  it('retorna 500 si no existe', async () => {
    citaService.getById.mockRejectedValue(new Error('Cita no encontrada'));
    const res = await request(app).get('/api/citas/99');
    expect(res.status).toBe(500);
  });
});

describe('GET /api/citas/paciente/:pacienteId', () => {
  it('retorna citas de un paciente', async () => {
    citaService.getByPacienteId.mockResolvedValue([mockCita]);
    const res = await request(app).get('/api/citas/paciente/100');
    expect(res.status).toBe(200);
    expect(res.body[0].pacienteId).toBe(100);
  });
});

describe('GET /api/citas/paciente/:pacienteId/paginado', () => {
  it('retorna citas paginadas', async () => {
    citaService.getByPacienteIdPaginado.mockResolvedValue(mockPaginado);
    const res = await request(app).get('/api/citas/paciente/100/paginado?page=0&size=10');
    expect(res.status).toBe(200);
    expect(res.body).toHaveProperty('content');
    expect(res.body.totalPages).toBe(1);
  });
});

describe('GET /api/citas/medico/:medicoId', () => {
  it('retorna citas de un médico', async () => {
    citaService.getByMedicoId.mockResolvedValue([mockCita]);
    const res = await request(app).get('/api/citas/medico/1');
    expect(res.status).toBe(200);
    expect(res.body[0].medicoId).toBe(1);
  });
});

describe('GET /api/citas/medico/:medicoId/horas-ocupadas', () => {
  it('retorna horas ocupadas con fecha válida', async () => {
    citaService.getHorasOcupadas.mockResolvedValue(['09:30', '11:00']);
    const res = await request(app).get('/api/citas/medico/1/horas-ocupadas?fecha=2025-06-25');
    expect(res.status).toBe(200);
    expect(res.body).toContain('09:30');
  });

  it('retorna 400 si no se pasa fecha', async () => {
    const res = await request(app).get('/api/citas/medico/1/horas-ocupadas');
    expect(res.status).toBe(400);
  });
});

describe('POST /api/citas/agendar', () => {
  it('agenda cita correctamente', async () => {
    citaService.agendarCita.mockResolvedValue(mockCita);
    const res = await request(app).post('/api/citas/agendar').send({
      cita: { pacienteId: 100, especialidad: 'Cardiología', fechaHora: '2025-06-25T09:30:00' },
      medicoId: 1,
    });
    expect(res.status).toBe(200);
    expect(res.body.estado).toBe('PROGRAMADA');
  });

  it('retorna 400 si falta cita o medicoId', async () => {
    const res = await request(app).post('/api/citas/agendar').send({ medicoId: 1 });
    expect(res.status).toBe(400);
  });
});

describe('PUT /api/citas/:id', () => {
  it('actualiza cita correctamente', async () => {
    citaService.update.mockResolvedValue({ ...mockCita, estado: 'ATENDIDA' });
    const res = await request(app).put('/api/citas/1').send({
      cita: { estado: 'ATENDIDA' }, medicoId: 1,
    });
    expect(res.status).toBe(200);
    expect(res.body.estado).toBe('ATENDIDA');
  });
});

describe('POST /api/citas/:id/cancelar', () => {
  it('cancela cita correctamente', async () => {
    citaService.cancelarCita.mockResolvedValue(mockCancelacion);
    const res = await request(app).post('/api/citas/1/cancelar').send({
      motivo: 'Paciente no puede asistir',
      canceladoPor: 'PACIENTE',
    });
    expect(res.status).toBe(200);
    expect(res.body.canceladoPor).toBe('PACIENTE');
  });
});

describe('DELETE /api/citas/:id', () => {
  it('elimina cita correctamente', async () => {
    citaService.remove.mockResolvedValue();
    const res = await request(app).delete('/api/citas/1');
    expect(res.status).toBe(200);
  });
});

// ===================== MÉDICOS =====================
describe('GET /api/citas/medicos', () => {
  it('retorna lista de médicos', async () => {
    medicoService.getAll.mockResolvedValue([mockMedico]);
    const res = await request(app).get('/api/citas/medicos');
    expect(res.status).toBe(200);
    expect(res.body[0].nombre).toBe('Dr. Pérez');
  });
});

describe('GET /api/citas/medicos/:id', () => {
  it('retorna médico por ID', async () => {
    medicoService.getById.mockResolvedValue(mockMedico);
    const res = await request(app).get('/api/citas/medicos/1');
    expect(res.status).toBe(200);
    expect(res.body.rut).toBe('12345678-9');
  });
});

describe('POST /api/citas/medicos', () => {
  it('crea médico correctamente', async () => {
    medicoService.create.mockResolvedValue(mockMedico);
    const res = await request(app).post('/api/citas/medicos').send(mockMedico);
    expect(res.status).toBe(200);
    expect(res.body.id).toBe(1);
  });
});

describe('PUT /api/citas/medicos/:id', () => {
  it('actualiza médico correctamente', async () => {
    medicoService.update.mockResolvedValue({ ...mockMedico, nombre: 'Dr. González' });
    const res = await request(app).put('/api/citas/medicos/1').send({ nombre: 'Dr. González' });
    expect(res.status).toBe(200);
    expect(res.body.nombre).toBe('Dr. González');
  });
});

describe('DELETE /api/citas/medicos/:id', () => {
  it('elimina médico correctamente', async () => {
    medicoService.remove.mockResolvedValue();
    const res = await request(app).delete('/api/citas/medicos/1');
    expect(res.status).toBe(200);
  });
});
