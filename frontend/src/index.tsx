import React from 'react';
import ReactDOM from 'react-dom';

import { Provider } from "react-redux";
import { ApolloClientOptions } from 'apollo-boost';
import { ApolloClient } from 'apollo-client';
import { ApolloProvider } from '@apollo/react-hooks'
import { createHttpLink } from 'apollo-link-http';
import { setContext } from 'apollo-link-context';
import { InMemoryCache } from 'apollo-cache-inmemory';
import configureStore from "./configureStore";
import Main from './main';

// This works in production/deployed as there is an ingress rule for /graphql that points to moneycolserver
const URL = process.env.NODE_ENV === 'production' ? "graphql" : "http://localhost:4000/graphql"
console.log("GraphQL uri is: ", URL)

const httpLink = createHttpLink({
  uri: URL,
});

const authLink = setContext((_, { headers }) => {
  // get the authentication token from local storage if it exists
  const token = localStorage.getItem("token");
  // return the headers to the context so httpLink can read them
  return {
    headers: {
      ...headers,
      authorization: token ? `Bearer ${token}` : "",
    }
  }
});

const apolloClientOptions: ApolloClientOptions<{}> = {
  link: authLink.concat(httpLink),
  cache: new InMemoryCache()
}

const client = new ApolloClient(apolloClientOptions);

const store = configureStore(client);

// https://www.howtographql.com/react-urql/5-authentication/
// https://www.apollographql.com/docs/react/data/mutations/#usemutation-api
const WrappedApp = (
  <ApolloProvider client={client}>
    <Provider store={store}>
      <Main />
    </Provider>
  </ApolloProvider>
);

ReactDOM.render(WrappedApp, document.getElementById('root'));
