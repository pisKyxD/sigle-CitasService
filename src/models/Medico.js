const { DataTypes } = require('sequelize');
const sequelize = require('../config/database');

const Medico = sequelize.define('Medico', {
  id: { type: DataTypes.BIGINT, autoIncrement: true, primaryKey: true },
  rut: { type: DataTypes.STRING, allowNull: false, unique: true },
  nombre: { type: DataTypes.STRING, allowNull: false },
  email: { type: DataTypes.STRING, allowNull: false, unique: true },
  especialidad: { type: DataTypes.STRING, allowNull: false },
  establecimientoId: { type: DataTypes.BIGINT, field: 'establecimiento_id' },
  activo: { type: DataTypes.BOOLEAN, defaultValue: true },
}, { tableName: 'medicos', timestamps: false });

module.exports = Medico;