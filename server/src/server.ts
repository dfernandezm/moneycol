import express from 'express';
import { ApolloServer, AuthenticationError } from 'apollo-server-express';
import depthLimit from 'graphql-depth-limit';
import { createServer } from 'http';
import compression from 'compression';
import cors from 'cors';
import schema from './schema';
import { CollectionsRestDatasource } from './infrastructure/collections/CollectionsRestDatasource';
import { authenticationService } from './infrastructure/authentication/AuthenticationService';
import jwt from 'jsonwebtoken';

const app = express();


const server = new ApolloServer({
  schema,
  context: ({ req }) => {

    // See: https://www.apollographql.com/docs/apollo-server/security/authentication/

    // Note! This example uses the `req` object to access headers,
    // but the arguments received by `context` vary by integration.
    // This means they will vary for Express, Koa, Lambda, etc.!
    //
    // To find out the correct arguments for a specific integration,
    // see the `context` option in the API reference for `apollo-server`:
    // https://www.apollographql.com/docs/apollo-server/api/apollo-server/

    // Get the user token from the headers.
    let token = req.headers.authorization || '';
    let user = {};

    if (token) {
      token = token.replace("Bearer", "").trim();
    }

    // add the user and token to the context as-is, it will be checked in the relevant parts
    return { user, token };
  },

  dataSources: () => {
    return {
      collectionsAPI: new CollectionsRestDatasource()
    };
  },
  validationRules: [depthLimit(7)],
});

app.use('*', cors());
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