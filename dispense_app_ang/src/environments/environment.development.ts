export const environment = {
  production: false,
  scheme:'http',
  keycloak: {
    url: 'http://localhost:8084',
    realm: 'DISP',
    clientId: 'etu-app',
  },
  resourceServer:{
    host: 'localhost',
    port: '9090',
    url:'http://localhost:9090',
  }
};
