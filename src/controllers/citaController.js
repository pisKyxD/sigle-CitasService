const service = require('../services/citaService');

const getAll = async (req, res, next) => {
  try { res.json(await service.getAll()); } catch (e) { next(e); }
};

const getById = async (req, res, next) => {
  try { res.json(await service.getById(req.params.id)); } catch (e) { next(e); }
};

const getByPacienteId = async (req, res, next) => {
  try { res.json(await service.getByPacienteId(req.params.pacienteId)); } catch (e) { next(e); }
};

const getByPacienteIdPaginado = async (req, res, next) => {
  try {
    const page = parseInt(req.query.page) || 0;
    const size = parseInt(req.query.size) || 10;
    res.json(await service.getByPacienteIdPaginado(req.params.pacienteId, page, size));
  } catch (e) { next(e); }
};

const getByMedicoId = async (req, res, next) => {
  try { res.json(await service.getByMedicoId(req.params.medicoId)); } catch (e) { next(e); }
};

const getHorasOcupadas = async (req, res, next) => {
  try {
    const { fecha } = req.query;
    if (!fecha) return res.status(400).json({ error: 'El parámetro fecha es obligatorio (YYYY-MM-DD)' });
    res.json(await service.getHorasOcupadas(req.params.medicoId, fecha));
  } catch (e) { next(e); }
};

const agendarCita = async (req, res, next) => {
  try {
    const { cita, medicoId } = req.body;
    if (!cita || !medicoId) return res.status(400).json({ error: 'cita y medicoId son obligatorios' });
    res.json(await service.agendarCita(cita, medicoId));
  } catch (e) { next(e); }
};

const update = async (req, res, next) => {
  try {
    const { cita, medicoId } = req.body;
    res.json(await service.update(req.params.id, cita || req.body, medicoId));
  } catch (e) { next(e); }
};

const cancelarCita = async (req, res, next) => {
  try {
    const { motivo, canceladoPor } = req.body;
    res.json(await service.cancelarCita(req.params.id, motivo, canceladoPor));
  } catch (e) { next(e); }
};

const remove = async (req, res, next) => {
  try { await service.remove(req.params.id); res.json({ message: 'Cita eliminada' }); } catch (e) { next(e); }
};

module.exports = {
  getAll, getById, getByPacienteId, getByPacienteIdPaginado,
  getByMedicoId, getHorasOcupadas, agendarCita, update, cancelarCita, remove,
};
