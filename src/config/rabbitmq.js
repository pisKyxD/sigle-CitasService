const amqp = require('amqplib');

const EXCHANGE = 'sigle.exchange';
const QUEUE = 'sigle.citas.canceladas';
const ROUTING_KEY = 'citas.cancelada';

const QUEUE_CREADA = 'sigle.citas.creadas';
const ROUTING_KEY_CREADA = 'citas.creada';

let channel = null;

async function connect() {
  try {
    console.log('[RabbitMQ] URL:', process.env.RABBITMQ_URL);
    const conn = await amqp.connect(process.env.RABBITMQ_URL);
    channel = await conn.createChannel();
    await channel.assertExchange(EXCHANGE, 'direct', { durable: true });
    await channel.assertQueue(QUEUE, { durable: true });
    await channel.bindQueue(QUEUE, EXCHANGE, ROUTING_KEY);
    await channel.assertQueue(QUEUE_CREADA, { durable: true });
    await channel.bindQueue(QUEUE_CREADA, EXCHANGE, ROUTING_KEY_CREADA);
    console.log('[RabbitMQ] Conectado y exchange configurado.');

    conn.on('close', () => {
      console.warn('[RabbitMQ] Conexión cerrada. Reintentando en 5s...');
      channel = null;
      setTimeout(connect, 5000);
    });
  } catch (err) {
    console.error('[RabbitMQ] No se pudo conectar:', err.message);
    setTimeout(connect, 5000);
  }
}

function publishCancelacion(evento) {
  if (!channel) {
    console.warn('[RabbitMQ] Sin canal disponible, no se publicó el evento.');
    return;
  }
  channel.publish(
    EXCHANGE,
    ROUTING_KEY,
    Buffer.from(JSON.stringify(evento)),
    { persistent: true }
  );
  console.log('[RabbitMQ] Evento de cancelación publicado:', evento.citaId);
}

function publishCreacion(evento) {
  if (!channel) {
    console.warn('[RabbitMQ] Sin canal disponible, no se publicó el evento.');
    return;
  }
  channel.publish(
    EXCHANGE,
    ROUTING_KEY_CREADA,
    Buffer.from(JSON.stringify(evento)),
    { persistent: true }
  );
  console.log('[RabbitMQ] Evento de creación de cita publicado:', evento.citaId);
}

module.exports = { connect, publishCancelacion, publishCreacion };