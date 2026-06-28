require('dotenv').config();
const app = require('./app');
const sequelize = require('./config/database');
const { connect } = require('./config/rabbitmq');

require('./models/Medico');
require('./models/Cita');
require('./models/Cancelacion');

const PORT = process.env.PORT || 8082;

sequelize.sync({ alter: true })
  .then(() => {
    console.log('[DB] Conectado y sincronizado.');
    connect();
    app.listen(PORT, () => console.log(`[Server] citas-service corriendo en puerto ${PORT}`));
  })
  .catch((err) => {
    console.error('[DB] Error al conectar:', err.message);
    process.exit(1);
  });
