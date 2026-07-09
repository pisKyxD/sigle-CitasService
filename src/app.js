require('dotenv').config();
const express = require('express');

const app = express();
app.use(express.json());

app.use('/api/citas/medicos', require('./routes/medicos'));
app.use('/api/citas', require('./routes/citas'));

app.get('/actuator/health', (req, res) => res.json({ status: 'UP', service: 'citas-service' }));

app.use((err, req, res, next) => {
  const status = err.status || 500;
  res.status(status).json({ error: err.message || 'Error interno del servidor' });
});

module.exports = app;
