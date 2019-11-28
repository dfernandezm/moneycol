import React from 'react';
import ReactDOM from 'react-dom';

import 'materialize-css/dist/css/materialize.min.css';
import './main.css';

import ApolloClient from 'apollo-boost';
import { ApolloProvider } from '@apollo/react-hooks'

import { Main } from './main';

const API_BASE_URL = process.env.REACT_APP_APOLLO_SERVER_URL || "localhost:4000"
const APOLLO_SERVER_URL = `http://${API_BASE_URL}/graphql`;

console.log("URI: ", APOLLO_SERVER_URL);

const client = new ApolloClient({
  uri: APOLLO_SERVER_URL,
});

const WrappedApp = (
  <ApolloProvider client={client}>
    <Main />
  </ApolloProvider>
);

ReactDOM.render(WrappedApp, document.getElementById('root'));
