import React from 'react';
import ReactDOM from 'react-dom';

// import 'materialize-css/dist/css/materialize.min.css';
// import './main.css';

import ApolloClient from 'apollo-boost';
import { ApolloProvider } from '@apollo/react-hooks'

import Main from './main';
import { Provider } from "react-redux";

import configureStore from "./configureStore";

// This works in production/deployed as there is an ingress rule for /graphql that points to moneycolserver
const URL = process.env.NODE_ENV === 'production' ? "graphql" : "http://localhost:4000/graphql"
console.log("GraphQL uri is: ", URL)

const client = new ApolloClient({
  uri: URL,
});

const store = configureStore();

const WrappedApp = (
  <ApolloProvider client={client}>
    <Provider store={store}>
      <Main />
    </Provider>
  </ApolloProvider>
);

ReactDOM.render(WrappedApp, document.getElementById('root'));
