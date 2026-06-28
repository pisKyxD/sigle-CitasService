const router = require('express').Router();
const ctrl = require('../controllers/citaController');

router.get('/', ctrl.getAll);
router.get('/paciente/:pacienteId/paginado', ctrl.getByPacienteIdPaginado);
router.get('/paciente/:pacienteId', ctrl.getByPacienteId);
router.get('/medico/:medicoId/horas-ocupadas', ctrl.getHorasOcupadas);
router.get('/medico/:medicoId', ctrl.getByMedicoId);
router.get('/:id', ctrl.getById);
router.post('/agendar', ctrl.agendarCita);
router.put('/:id', ctrl.update);
router.post('/:id/cancelar', ctrl.cancelarCita);
router.delete('/:id', ctrl.remove);

module.exports = router;
