# MoneyCol Frontend

This is the frontend of the system. Written in:
- React
- Typescript 
- Graphql
- `create-react-app`
- CREMA template

## Run with CREMA template

CREMA template has been used and added as a `git submodule` (https://git-scm.com/book/en/v2/Git-Tools-Submodules) in order to keep it in a separate repo to get updates.


### Update the submodule

```
cd crema_template-2.0
git submodule update --remote --rebase
```

### Push changes to Submodule

Just move into the folder, commit and push to the right branch as usual from inside the submodule directory. Once back into the main folder, the push can be made doing a check into the submodule.

```
git push --recurse-submodules=check
```

### Change the branch of the submodule

It's a little confusing to get used to this, but submodules are not on a branch. They are, just a pointer to a particular commit of the submodule's repository (`crema_template-2.0` in this case).

This means, when someone else checks out the repository, or pulls the code, and the does `git submodule update`, the submodule is checked out to that particular commit (the latest it was pushed to).

This is great for a submodule that does not change often, because then everyone on the project can have the submodule at the same commit.

If you want to move the submodule to a particular tag/commit/branch:

```
cd submodule_directory
git checkout v1.0
cd ..
git add submodule_directory
git commit -m "moved submodule to v1.0"
git push
```

Then, another developer who wants to have `submodule_directory` changed to that tag, does:

```
git pull
git submodule update --init
```

git pull changes which commit their submodule directory points to, `git submodule update` actually merges in the new code.

## Run locally without backend

Just run `npm start`. That would start the Webpack dev server, but the GraphQL queries won't work. Use it for styling and static development.

## Run locally with backend

### Elasticsearch

* Unzip the `data.zip` folder in a known location in your machine
* Start the backend server (more details in [server startup](https://github.com/dfernandezm/moneycol/server/README.md))
```
$ cd server
$ ELASTICSEARCH_ENDPOINT_WITH_PORT=localhost:9200 npm run start:dev
```

* Open file `src/index.tsx` and change `uri` to `localhost:4000/graphql`. Reload browser and server calls should work.

## Some resources and troubleshooting

### Typescript migration resources:

* https://stackoverflow.com/questions/47508564/migrating-create-react-app-from-javascript-to-typescript/47674979#47674979
* https://markpollmann.com/react-moving-to-typescript
* https://github.com/typescript-cheatsheets/react-typescript-cheatsheet

### React, Apollo, Hooks and GQL setup:

* Introduction of Apollo Client in React: https://www.apollographql.com/docs/react/get-started/
* Setting up Apollo with Typescript in React example: https://github.com/apollographql/react-apollo/tree/master/examples/typescript
* Use Apollo Client directly in event handlers: https://stackoverflow.com/questions/55890604/how-to-implement-search-function-in-react-graphql-using-react-hooks-and-apollo-c

### Making GQL calls with Apollo Client instead of `useQuery`

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
