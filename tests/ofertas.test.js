const request = require('supertest');

jest.mock('../src/services/ofertaService');
jest.mock('../src/config/rabbitmq', () => ({
  connect: jest.fn(), publishCancelacion: jest.fn(), publishCreacion: jest.fn(), publishOfertaCreada: jest.fn(),
}));

const ofertaService = require('../src/services/ofertaService');
const app = require('../src/app');

const mockOferta = {
  id: 1, citaCanceladaId: 10, listaEsperaId: 5, pacienteId: 200, medicoId: 1,
  especialidad: 'Cardiología', estado: 'PENDIENTE', fechaOferta: new Date(),
  fechaExpiracion: new Date(Date.now() + 3600000),
};
const mockCitaNueva = { id: 55, pacienteId: 200, medicoId: 1, especialidad: 'Cardiología', estado: 'PROGRAMADA' };

describe('GET /api/citas/oferta/:id', () => {
  it('retorna la oferta por id', async () => {
    ofertaService.getById.mockResolvedValue(mockOferta);
    const res = await request(app).get('/api/citas/oferta/1');
    expect(res.status).toBe(200);
    expect(res.body.estado).toBe('PENDIENTE');
  });

  it('retorna 404 si no existe', async () => {
    ofertaService.getById.mockImplementation(() => { const e = new Error('Oferta no encontrada'); e.status = 404; throw e; });
    const res = await request(app).get('/api/citas/oferta/999');
    expect(res.status).toBe(404);
  });
});

describe('POST /api/citas/oferta/:id/confirmar', () => {
  it('confirma la oferta y crea la cita real', async () => {
    ofertaService.confirmar.mockResolvedValue(mockCitaNueva);
    const res = await request(app).post('/api/citas/oferta/1/confirmar');
    expect(res.status).toBe(200);
    expect(res.body.id).toBe(55);
  });

  it('retorna 409 si la oferta ya no está pendiente', async () => {
    ofertaService.confirmar.mockImplementation(() => {
      const e = new Error('Esta oferta ya no está disponible'); e.status = 409; throw e;
    });
    const res = await request(app).post('/api/citas/oferta/1/confirmar');
    expect(res.status).toBe(409);
  });
});

describe('POST /api/citas/oferta/:id/rechazar', () => {
  it('rechaza la oferta y dispara la reasignación al siguiente candidato', async () => {
    ofertaService.rechazar.mockResolvedValue({ ...mockOferta, estado: 'RECHAZADA' });
    const res = await request(app).post('/api/citas/oferta/1/rechazar');
    expect(res.status).toBe(200);
    expect(res.body.estado).toBe('RECHAZADA');
  });
});