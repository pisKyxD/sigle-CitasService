const cron = require('node-cron');
const ofertaService = require('../services/ofertaService');

function iniciarJobExpiracionOfertas() {
  cron.schedule('*/10 * * * *', async () => {
    try {
      const cantidad = await ofertaService.expirarPendientesVencidas();
      if (cantidad > 0) console.log(`[Job Expiración] ${cantidad} oferta(s) cascadeadas.`);
    } catch (err) {
      console.error('[Job Expiración] Error:', err.message);
    }
  });
  console.log('[Job Expiración] Job programado (cada 10 min).');
}

module.exports = { iniciarJobExpiracionOfertas };