const service = require('../services/medicoService');

const getAll = async (req, res, next) => {
  try { res.json(await service.getAll()); } catch (e) { next(e); }
};

const getById = async (req, res, next) => {
  try { res.json(await service.getById(req.params.id)); } catch (e) { next(e); }
};

const getByEmail = async (req, res, next) => {
  try { res.json(await service.getByEmail(req.params.email)); } catch (e) { next(e); }
};

const create = async (req, res, next) => {
  try { res.json(await service.create(req.body)); } catch (e) { next(e); }
};

const update = async (req, res, next) => {
  try { res.json(await service.update(req.params.id, req.body)); } catch (e) { next(e); }
};

const remove = async (req, res, next) => {
  try { await service.remove(req.params.id); res.json({ message: 'Médico eliminado' }); } catch (e) { next(e); }
};

module.exports = { getAll, getById, getByEmail, create, update, remove };