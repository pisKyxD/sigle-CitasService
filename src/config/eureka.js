const { Eureka } = require('eureka-js-client');

const PORT = process.env.PORT || 10000;
const EUREKA_HOST = process.env.EUREKA_HOST || 'localhost';
const EUREKA_PORT = process.env.EUREKA_PORT || 8761;
const INSTANCE_HOST = process.env.INSTANCE_HOST || 'localhost';

const client = new Eureka({
  instance: {
    app: 'citasservice',
    instanceId: `citasservice:${PORT}`,
    hostName: INSTANCE_HOST,
    ipAddr: INSTANCE_HOST,
    port: {
      '$': PORT,
      '@enabled': true,
    },
    vipAddress: 'citasservice',
    statusPageUrl: `http://${INSTANCE_HOST}:${PORT}/actuator/health`,
    healthCheckUrl: `http://${INSTANCE_HOST}:${PORT}/actuator/health`,
    dataCenterInfo: {
      '@class': 'com.netflix.appinfo.InstanceInfo$DefaultDataCenterInfo',
      name: 'MyOwn',
    },
  },
  eureka: {
    host: EUREKA_HOST,
    port: EUREKA_PORT,
    servicePath: '/eureka/apps/',
  },
});

module.exports = client;