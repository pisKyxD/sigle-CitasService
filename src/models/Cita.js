const { DataTypes } = require('sequelize');
const sequelize = require('../config/database');
const Medico = require('./Medico');

const Cita = sequelize.define('Cita', {
  id: { type: DataTypes.BIGINT, autoIncrement: true, primaryKey: true },
  pacienteId: { type: DataTypes.BIGINT, field: 'paciente_id' },
  listaEsperaId: { type: DataTypes.BIGINT, field: 'lista_espera_id' },
  medicoId: { type: DataTypes.BIGINT, field: 'medico_id' },
  especialidad: { type: DataTypes.STRING },
  fechaHora: { type: DataTypes.DATE, field: 'fecha_hora' },
  estado: {
    type: DataTypes.ENUM('PROGRAMADA', 'CANCELADA', 'ATENDIDA', 'REPROGRAMADA'),
    defaultValue: 'PROGRAMADA'
  },
}, { tableName: 'citas', timestamps: false });

Cita.belongsTo(Medico, { foreignKey: 'medico_id', as: 'medico' });
Medico.hasMany(Cita, { foreignKey: 'medico_id', as: 'citas' });

module.exports = Cita;
