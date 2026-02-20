export const environment = {
  production: true,
  scheme:'https',
  keycloak: {
    url: 'https://localhost:8084',
    realm: 'DISP',
    clientId: 'etu-app',
  },
  resourceServer:{
    host: 'localhost',
    port: '9090',
    url:'https://localhost:9090',
  }

};
