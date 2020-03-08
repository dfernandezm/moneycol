import express from 'express';
import { ApolloServer } from 'apollo-server-express';
import depthLimit from 'graphql-depth-limit';
import { createServer } from 'http';
import compression from 'compression';
import cors from 'cors';
import schema from './schema';
import { CollectionsRestDatasource } from './infrastructure/collections/CollectionsRestDatasource';

const app = express();

//TODO: extract this to secret or similar
const FIREBASE_CONFIG = {
  apiKey: "AIzaSyDImTA3-o5ew92DQ4pg0-nVKTHR92ncq-U",
  authDomain: "moneycol.firebaseapp.com",
  databaseURL: "https://moneycol.firebaseio.com",
  projectId: "moneycol",
  storageBucket: "moneycol.appspot.com",
  messagingSenderId: "461081581931",
  appId: "1:461081581931:web:3ca5344ae0e1df6dfa542e"
};

const server = new ApolloServer({
  schema,
  dataSources: () => {
    return {
      collectionsAPI: new CollectionsRestDatasource()
    };
  },
  validationRules: [depthLimit(7)],
});

app.use('*', cors());
app.use(compression());

app.get('/api/firebaseConfig', (req, res) => {
  res.json(FIREBASE_CONFIG);
});

server.applyMiddleware({ app, path: '/graphql' });

const httpServer = createServer(app);

httpServer.listen(
  { port: 4000 },
  (): void => {
    console.log(`\nGraphQL is now running on http://localhost:4000/graphql`);
    console.log(
      `Try your health check at:  http://localhost:4000/.well-known/apollo/server-health`);
  });