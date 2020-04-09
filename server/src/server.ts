import express from 'express';
import { ApolloServer, AuthenticationError } from 'apollo-server-express';
import depthLimit from 'graphql-depth-limit';
import { createServer } from 'http';
import compression from 'compression';
import cors from 'cors';
import schema from './schema';
import { CollectionsRestDatasource } from './infrastructure/collections/CollectionsRestDatasource';
import jwt from 'jsonwebtoken';

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
    if (token) {
      token = token.replace("Bearer","").trim();
      validateToken(token)
    }

    //TODO: try to retrieve a user with the token
    const user = {};
 
    // add the user and token to the context
    return { user, token };
  },
  dataSources: () => {
    return {
      collectionsAPI: new CollectionsRestDatasource()
    };
  },
  validationRules: [depthLimit(7)],
});

// This should be done only in the cases that tokens / user-centric functionality is required
const validateToken = (token: string) => {

    //TODO: should verify the signature or just forward instead of decoding
    //see: https://firebase.google.com/docs/auth/admin/verify-id-tokens
    const decoded: any = jwt.decode(token);

    /*
    { iss: 'https://securetoken.google.com/moneycol',
      aud: 'moneycol',
      auth_time: 1586519807,
      user_id: '3eiK7CqInPbgcw1LYq1S8sJqGLy2',
      sub: '...',
      iat: 1586523227,
      exp: 1586526827,
      email: 'morenza@gmail.com',
      email_verified: true,
      firebase:
      { identities: { email: [Array] }, sign_in_provider: 'password' } }
     */
    if (decoded && decoded.aud == "moneycol") {
      console.log(`Valid token has been received, user ID is: ${decoded.user_id}`);
    } else {
      throw new AuthenticationError("Invalid token has been provided");
    };
}

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