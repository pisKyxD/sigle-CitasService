const OfertaCupo = require('../models/OfertaCupo');
const Cita = require('../models/Cita');
const Medico = require('../models/Medico');
const listasClient = require('./listasClient');
const { publishOfertaCreada } = require('../config/rabbitmq');

const HORAS_PARA_EXPIRAR = 2;

const getById = async (id) => {
  const oferta = await OfertaCupo.findByPk(id);
  if (!oferta) { const e = new Error('Oferta no encontrada'); e.status = 404; throw e; }
  return oferta;
};

const buscarYOfertar = async (citaCancelada, excluirListaIds = []) => {
  const especialidad = citaCancelada.especialidad;
  if (!especialidad) return null;

  const candidato = await listasClient.claimCandidato(especialidad, excluirListaIds);
  if (!candidato) {
    console.log(`[Ofertas] Sin candidatos en espera para "${especialidad}", cupo queda libre.`);
    return null;
  }

  const ahora = new Date();
  const expiracion = new Date(ahora.getTime() + HORAS_PARA_EXPIRAR * 60 * 60 * 1000);

  const oferta = await OfertaCupo.create({
    citaCanceladaId: citaCancelada.id, listaEsperaId: candidato.id, pacienteId: candidato.pacienteId,
    medicoId: citaCancelada.medicoId, especialidad, fechaHora: citaCancelada.fechaHora,
    estado: 'PENDIENTE', fechaOferta: ahora, fechaExpiracion: expiracion,
  });

  const medico = await Medico.findByPk(citaCancelada.medicoId);

  publishOfertaCreada({
    ofertaId: oferta.id, pacienteId: candidato.pacienteId, especialidad,
    fechaHora: citaCancelada.fechaHora, medicoNombre: medico?.nombre, fechaExpiracion: expiracion,
  });

  console.log(`[Ofertas] Cupo de "${especialidad}" ofrecido a lista_espera#${candidato.id}, expira ${expiracion.toISOString()}`);
  return oferta;
};

const confirmar = async (ofertaId) => {
  const citaService = require('./citaService');
  const oferta = await getById(ofertaId);

  if (oferta.estado !== 'PENDIENTE') {
    const e = new Error(`Esta oferta ya no está disponible (estado actual: ${oferta.estado})`); e.status = 409; throw e;
  }
  if (new Date() > new Date(oferta.fechaExpiracion)) {
    await oferta.update({ estado: 'EXPIRADA', fechaRespuesta: new Date() });
    const e = new Error('El plazo para confirmar este cupo ya expiró'); e.status = 409; throw e;
  }

  const citaNueva = await citaService.agendarCita(
    { pacienteId: oferta.pacienteId, listaEsperaId: oferta.listaEsperaId, fechaHora: oferta.fechaHora, especialidad: oferta.especialidad },
    oferta.medicoId
  );

  await oferta.update({ estado: 'CONFIRMADA', fechaRespuesta: new Date(), citaNuevaId: citaNueva.id });
  await listasClient.resolverOferta(oferta.listaEsperaId, 'AGENDADO');
  return citaNueva;
};

const rechazarOExpirar = async (ofertaId, motivo) => {
  const oferta = await getById(ofertaId);
  if (oferta.estado !== 'PENDIENTE') return oferta;

  await oferta.update({ estado: motivo, fechaRespuesta: new Date() });
  await listasClient.resolverOferta(oferta.listaEsperaId, 'ESPERA');

  const citaCancelada = await Cita.findByPk(oferta.citaCanceladaId);
  if (!citaCancelada) return oferta;

  const yaOfrecidos = await OfertaCupo.findAll({ where: { citaCanceladaId: oferta.citaCanceladaId }, attributes: ['listaEsperaId'] });
  await buscarYOfertar(citaCancelada, yaOfrecidos.map(o => o.listaEsperaId));
  return oferta;
};

const rechazar = async (ofertaId) => rechazarOExpirar(ofertaId, 'RECHAZADA');

const expirarPendientesVencidas = async () => {
  const vencidas = await OfertaCupo.findAll({ where: { estado: 'PENDIENTE' } });
  const ahora = new Date();
  const realmenteVencidas = vencidas.filter(o => new Date(o.fechaExpiracion) < ahora);
  for (const oferta of realmenteVencidas) {
    console.log(`[Ofertas] Oferta #${oferta.id} expiró, cascadeando al siguiente candidato.`);
    await rechazarOExpirar(oferta.id, 'EXPIRADA');
  }
  return realmenteVencidas.length;
};

module.exports = { getById, buscarYOfertar, confirmar, rechazar, expirarPendientesVencidas };   