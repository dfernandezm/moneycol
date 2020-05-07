import React from 'react';
import ReactDOM from 'react-dom';

// import { Provider } from "react-redux";
import { ApolloClientOptions } from 'apollo-boost';
import { ApolloClient } from 'apollo-client';
import { ApolloProvider } from '@apollo/react-hooks'
import { createHttpLink } from 'apollo-link-http';
import { setContext } from 'apollo-link-context';
import { InMemoryCache } from 'apollo-cache-inmemory';
//import configureStore from "./configureStore";
//import Main from './main';
import MainGql from './mainGql';

// This works in production/deployed as there is an ingress rule for /graphql that points to moneycolserver
const URL = process.env.NODE_ENV === 'production' ? "graphql" : "http://localhost:4000/graphql"
console.log("GraphQL uri is: ", URL)

const httpLink = createHttpLink({
  uri: URL,
});

const authLink = setContext((_, { headers }) => {
  // get the authentication token from local storage if it exists
  const token = localStorage.getItem('token');
  console.log("Token in local storage: ", token);
  // return the headers to the context so httpLink can read them
  //TODO: don't send the token if login mutation
  return {
    headers: {
      ...headers,
      authorization: token && token !== 'undefined' ? `Bearer ${token}` : "",
    }
  }
});

const apolloClientOptions: ApolloClientOptions<{}> = {
  link: authLink.concat(httpLink),
  cache: new InMemoryCache()
}

const client = new ApolloClient(apolloClientOptions);

//const store = configureStore();
// TODO: call verify in GQL, extract to function
const isAuthenticated = !!localStorage.getItem("token");

// https://www.howtographql.com/react-urql/5-authentication/
// https://www.apollographql.com/docs/react/data/mutations/#usemutation-api
// const WrappedAppRedux = (
//   <ApolloProvider client={client}>
//     <Provider store={store}>
//       <Main />
//     </Provider>

//   </ApolloProvider>
// );

const WrappedAppGql = (
  <ApolloProvider client={client}>
      <MainGql isAuthenticated={isAuthenticated}/>
  </ApolloProvider>
);

ReactDOM.render(WrappedAppGql, document.getElementById('root'));
