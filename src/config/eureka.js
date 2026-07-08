const { Eureka } = require('eureka-js-client');

const PORT = process.env.PORT || 8080;
const EUREKA_HOST = process.env.EUREKA_HOST || 'localhost';
const EUREKA_PORT = process.env.EUREKA_PORT || 8761;
const INSTANCE_HOST = process.env.INSTANCE_HOST || 'localhost';

// En Render (y en cualquier despliegue con dominio propio) el servicio solo
// es alcanzable desde afuera por HTTPS en el puerto 443; el puerto interno
// que usa la app (PORT) no está expuesto públicamente. Por eso, si no es
// localhost, hay que anunciar en Eureka el puerto seguro 443 y deshabilitar
// el puerto plano — si no, el ApiGateway intenta conectarse directo a
// "host:PORT" y la petición se cae por timeout.
const ES_DESPLIEGUE_REMOTO = INSTANCE_HOST !== 'localhost';

const client = new Eureka({
  instance: {
    app: 'citasservice',
    instanceId: `citasservice:${PORT}`,
    hostName: INSTANCE_HOST,
    ipAddr: INSTANCE_HOST,
    port: {
      '$': PORT,
      '@enabled': !ES_DESPLIEGUE_REMOTO,
    },
    securePort: {
      '$': 443,
      '@enabled': ES_DESPLIEGUE_REMOTO,
    },
    vipAddress: 'citasservice',
    secureVipAddress: 'citasservice',
    statusPageUrl: `https://${INSTANCE_HOST}/actuator/health`,
    healthCheckUrl: `https://${INSTANCE_HOST}/actuator/health`,
    dataCenterInfo: {
      '@class': 'com.netflix.appinfo.InstanceInfo$DefaultDataCenterInfo',
      name: 'MyOwn',
    },
  },
  eureka: {
    host: EUREKA_HOST,
    port: EUREKA_PORT,
    servicePath: '/eureka/apps/',
    ssl: true,
  },
});

module.exports = client;