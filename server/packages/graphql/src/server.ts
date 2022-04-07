import express from 'express';
import { ApolloServer } from 'apollo-server-express';
import depthLimit from 'graphql-depth-limit';
import { createServer } from 'http';
import compression from 'compression';
import cors from 'cors';
import schema from './schema';
import { CollectionsRestDatasource } from './infrastructure/collections/CollectionsRestDatasource';
import { tokenHelper } from './tokenHelper';

const app = express();
const collectionsApiDatasource = new CollectionsRestDatasource();
const PLAYGROUND_INTROSPECTION_QUERY = "IntrospectionQuery";

const server = new ApolloServer({
  schema,
  context: ({ req }) => {

    // Playground polls every 2 seconds for schema changes, this can be changed
    // in the settings
    if (req.body.operationName !== PLAYGROUND_INTROSPECTION_QUERY) {
      return tokenHelper.extractTokenFromRequest(req);
    }

    return {};
  },

  dataSources: () => {
    return {
      collectionsAPI: collectionsApiDatasource
    };
  },
  validationRules: [depthLimit(7)],
});

app.use(cors);
app.use(compression());

server.applyMiddleware({ app, path: '/graphql' });

const httpServer = createServer(app);

httpServer.listen(
  { port: 4000 },
  (): void => {
    console.log(`\nGraphQL is now running on http://localhost:4000/graphql`);
    console.log(
      `Try your health check at:  http://localhost:4000/.well-known/apollo/server-health`);
  });