## MoneyCol

### Migrate to Typescript:

* https://stackoverflow.com/questions/47508564/migrating-create-react-app-from-javascript-to-typescript/47674979#47674979
* https://markpollmann.com/react-moving-to-typescript
* https://github.com/typescript-cheatsheets/react-typescript-cheatsheet

### React, Apollo, Hooks and GQL setup:

* Introduction of Apollo Client in React: https://www.apollographql.com/docs/react/get-started/
* Setting up Apollo with Typescript in React example: https://github.com/apollographql/react-apollo/tree/master/examples/typescript
* Use Apollo Client directly in event handlers: https://stackoverflow.com/questions/55890604/how-to-implement-search-function-in-react-graphql-using-react-hooks-and-apollo-c

### Making GQL calls with Apollo Client instead of `useQuery`

```
// From inside a component controlled by ApolloProvider


const client = useApolloClient();
...
const performSearchCallWithApolloClient = async (client: ApolloClient<object>) => {
  console.log("Making call to GQL using apollo client");
    const { data } = await client.query({
    query: GQL_QUERY,
     variables: { searchTerm: usableSearchTerm },
  });
}
```