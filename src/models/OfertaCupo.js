const { DataTypes } = require('sequelize');
const sequelize = require('../config/database');
const Cita = require('./Cita');

const OfertaCupo = sequelize.define('OfertaCupo', {
  id: { type: DataTypes.BIGINT, autoIncrement: true, primaryKey: true },
  citaCanceladaId: { type: DataTypes.BIGINT, field: 'cita_cancelada_id' },
  listaEsperaId: { type: DataTypes.BIGINT, field: 'lista_espera_id' },
  pacienteId: { type: DataTypes.BIGINT, field: 'paciente_id' },
  medicoId: { type: DataTypes.BIGINT, field: 'medico_id' },
  especialidad: { type: DataTypes.STRING },
  fechaHora: { type: DataTypes.DATE, field: 'fecha_hora' },
  estado: { type: DataTypes.ENUM('PENDIENTE', 'CONFIRMADA', 'EXPIRADA', 'RECHAZADA'), defaultValue: 'PENDIENTE' },
  fechaOferta: { type: DataTypes.DATE, field: 'fecha_oferta' },
  fechaExpiracion: { type: DataTypes.DATE, field: 'fecha_expiracion' },
  fechaRespuesta: { type: DataTypes.DATE, field: 'fecha_respuesta', allowNull: true },
  citaNuevaId: { type: DataTypes.BIGINT, field: 'cita_nueva_id', allowNull: true },
}, { tableName: 'ofertas_cupo', timestamps: false });

OfertaCupo.belongsTo(Cita, { foreignKey: 'cita_cancelada_id', as: 'citaCancelada' });

module.exports = OfertaCupo;