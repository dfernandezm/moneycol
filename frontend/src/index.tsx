import React from 'react';
import ReactDOM from 'react-dom';

import 'materialize-css/dist/css/materialize.min.css';
import './main.css';

import ApolloClient from 'apollo-boost';
import { ApolloProvider } from '@apollo/react-hooks'

import Main from './main';

const URL = process.env.NODE_ENV == 'production' ? "graphql" : "http://localhost:4000/graphql"
console.log("GraphQL uri is: ", URL)

const client = new ApolloClient({
  uri: URL,
});

const WrappedApp = (
  <ApolloProvider client={client}>
    <Main />
  </ApolloProvider>
);

ReactDOM.render(WrappedApp, document.getElementById('root'));
