const { Op } = require('sequelize');
const Cita = require('../models/Cita');
const Cancelacion = require('../models/Cancelacion');
const Medico = require('../models/Medico');
const medicoService = require('./medicoService');
const { publishCancelacion } = require('../config/rabbitmq');

const ESTADOS_VALIDOS = ['PROGRAMADA', 'CANCELADA', 'ATENDIDA', 'REPROGRAMADA'];
const CANCELADO_POR_VALIDO = ['PACIENTE', 'MEDICO', 'ADMINISTRACION'];

const getAll = async () => await Cita.findAll({ include: [{ model: Medico, as: 'medico' }] });

const getById = async (id) => {
  const c = await Cita.findByPk(id, { include: [{ model: Medico, as: 'medico' }] });
  if (!c) { const e = new Error('Cita no encontrada'); e.status = 404; throw e; }
  return c;
};

const getByPacienteId = async (pacienteId) =>
  await Cita.findAll({ where: { pacienteId }, include: [{ model: Medico, as: 'medico' }] });

const getByPacienteIdPaginado = async (pacienteId, page = 0, size = 10) => {
  const offset = page * size;
  const { count, rows } = await Cita.findAndCountAll({
    where: { pacienteId },
    include: [{ model: Medico, as: 'medico' }],
    limit: size,
    offset,
  });
  return {
    content: rows,
    totalElements: count,
    totalPages: Math.ceil(count / size),
    currentPage: page,
  };
};

const getByMedicoId = async (medicoId) =>
  await Cita.findAll({ where: { medicoId }, include: [{ model: Medico, as: 'medico' }] });

const getHorasOcupadas = async (medicoId, fecha) => {
  const inicio = new Date(`${fecha}T00:00:00`);
  const fin = new Date(`${fecha}T23:59:59`);
  const citas = await Cita.findAll({
    where: {
      medicoId,
      fechaHora: { [Op.between]: [inicio, fin] },
      estado: { [Op.ne]: 'CANCELADA' },
    },
  });
  return citas.map(c => {
    const d = new Date(c.fechaHora);
    return `${String(d.getHours()).padStart(2, '0')}:${String(d.getMinutes()).padStart(2, '0')}`;
  });
};

const agendarCita = async (citaData, medicoId) => {
  if (!citaData.pacienteId || !medicoId) {
    const e = new Error('pacienteId y medicoId son obligatorios'); e.status = 400; throw e;
  }
  const medico = await medicoService.getById(medicoId);
  const cita = await Cita.create({
    pacienteId: citaData.pacienteId,
    listaEsperaId: citaData.listaEsperaId,
    medicoId: medico.id,
    especialidad: citaData.especialidad || medico.especialidad,
    fechaHora: citaData.fechaHora,
    estado: 'PROGRAMADA',
  });
  return getById(cita.id);
};

const update = async (id, citaData, medicoId) => {
  const cita = await Cita.findByPk(id);
  if (!cita) { const e = new Error('Cita no encontrada'); e.status = 404; throw e; }
  const updates = {};
  if (medicoId) {
    const medico = await medicoService.getById(medicoId);
    updates.medicoId = medico.id;
  }
  if (citaData.especialidad) updates.especialidad = citaData.especialidad;
  if (citaData.fechaHora) updates.fechaHora = citaData.fechaHora;
  if (citaData.estado && ESTADOS_VALIDOS.includes(citaData.estado)) updates.estado = citaData.estado;
  await cita.update(updates);
  return getById(id);
};

const cancelarCita = async (id, motivo, canceladoPor) => {
  if (!motivo) { const e = new Error('El motivo es obligatorio'); e.status = 400; throw e; }
  if (!CANCELADO_POR_VALIDO.includes(canceladoPor)) {
    const e = new Error(`canceladoPor debe ser: ${CANCELADO_POR_VALIDO.join(', ')}`); e.status = 400; throw e;
  }

  const cita = await Cita.findByPk(id);
  if (!cita) { const e = new Error('Cita no encontrada'); e.status = 404; throw e; }

  await cita.update({ estado: 'CANCELADA' });

  const cancelacion = await Cancelacion.create({
    citaId: cita.id,
    motivo,
    canceladoPor,
    fechaCancelacion: new Date(),
    reasignado: false,
  });

  // Publicar evento en RabbitMQ
  publishCancelacion({
    citaId: cita.id,
    pacienteId: cita.pacienteId,
    listaEsperaId: cita.listaEsperaId,
    motivo,
  });

  return cancelacion;
};

const remove = async (id) => {
  const cita = await Cita.findByPk(id);
  if (!cita) { const e = new Error('Cita no encontrada'); e.status = 404; throw e; }
  await cita.destroy();
};

module.exports = {
  getAll, getById, getByPacienteId, getByPacienteIdPaginado,
  getByMedicoId, getHorasOcupadas, agendarCita, update, cancelarCita, remove,
};
