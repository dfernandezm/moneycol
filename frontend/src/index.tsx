import React from 'react';
import ReactDOM from 'react-dom';

import 'materialize-css/dist/css/materialize.min.css';
import './main.css';

import ApolloClient from 'apollo-boost';
//import { createHttpLink } from 'apollo-link-http';
//import { InMemoryCache } from 'apollo-cache-inmemory';
import { ApolloProvider } from '@apollo/react-hooks'


import { Main } from './main';

// const httpLink = createHttpLink({
//   uri: 'http://localhost:4000/graphql'
// });

// const client = new ApolloClient({
//   cache: new InMemoryCache(),
//   link: httpLink
// });

const client = new ApolloClient({
  uri: 'http://localhost:4000/graphql',
});

const WrappedApp = (
  <ApolloProvider client={client}>
    <Main />
  </ApolloProvider>
);

ReactDOM.render(WrappedApp, document.getElementById('root'));
