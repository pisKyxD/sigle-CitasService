const Medico = require('../models/Medico');

const getAll = async () => await Medico.findAll();

const getById = async (id) => {
  const m = await Medico.findByPk(id);
  if (!m) { const e = new Error('Médico no encontrado'); e.status = 404; throw e; }
  return m;
};

const create = async (data) => {
  if (!data.rut || !data.nombre || !data.especialidad || !data.establecimientoId) {
    const e = new Error('rut, nombre, especialidad y establecimientoId son obligatorios');
    e.status = 400; throw e;
  }
  return await Medico.create(data);
};

const update = async (id, data) => {
  const m = await getById(id);
  await m.update({
    rut: data.rut,
    nombre: data.nombre,
    especialidad: data.especialidad,
    establecimientoId: data.establecimientoId,
    activo: data.activo,
  });
  return m;
};

const remove = async (id) => {
  const m = await getById(id);
  await m.destroy();
};

module.exports = { getAll, getById, create, update, remove };
