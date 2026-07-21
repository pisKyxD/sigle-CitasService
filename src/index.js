if (process.env.NODE_ENV !== 'production') {
  require('dotenv').config();
}
const app = require('./app');
const sequelize = require('./config/database');
const { connect } = require('./config/rabbitmq');
const eurekaClient = require('./config/eureka');
const { iniciarJobExpiracionOfertas } = require('./jobs/expiracionOfertas');

require('./models/OfertaCupo');
require('./models/Medico');
require('./models/Cita');
require('./models/Cancelacion');

const PORT = process.env.PORT || 10000;

sequelize.sync({ force: false })
  .then(() => {
    console.log('[DB] Conectado y sincronizado.');
    connect();
    app.listen(PORT, () => {
      console.log(`[Server] citas-service corriendo en puerto ${PORT}`);
      iniciarJobExpiracionOfertas();
      eurekaClient.start((error) => {
        if (error) {
          console.error('[Eureka] Error al registrar:', error);
        } else {
          console.log('[Eureka] citas-service registrado correctamente');
        }
      });
    });
  })
  .catch((err) => {
    console.error('[DB] Error al conectar:', err.message);
    process.exit(1);
  });

process.on('SIGINT', () => {
  eurekaClient.stop(() => {
    console.log('[Eureka] Desregistrado');
    process.exit(0);
  });
});