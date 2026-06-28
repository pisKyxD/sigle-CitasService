const { DataTypes } = require('sequelize');
const sequelize = require('../config/database');
const Cita = require('./Cita');

const Cancelacion = sequelize.define('Cancelacion', {
  id: { type: DataTypes.BIGINT, autoIncrement: true, primaryKey: true },
  citaId: { type: DataTypes.BIGINT, field: 'cita_id' },
  motivo: { type: DataTypes.STRING },
  canceladoPor: {
    type: DataTypes.ENUM('PACIENTE', 'MEDICO', 'ADMINISTRACION'),
    field: 'cancelado_por'
  },
  fechaCancelacion: { type: DataTypes.DATE, field: 'fecha_cancelacion' },
  reasignado: { type: DataTypes.BOOLEAN, defaultValue: false },
}, { tableName: 'cancelaciones', timestamps: false });

Cancelacion.belongsTo(Cita, { foreignKey: 'cita_id', as: 'cita' });

module.exports = Cancelacion;
