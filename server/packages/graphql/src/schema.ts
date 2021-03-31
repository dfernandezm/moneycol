import 'graphql-import-node';
//import * as typeDefs from './schema/schema.graphql';
import typeDefs from './schema/typedefs';
import { makeExecutableSchema } from 'graphql-tools';
import resolvers from './resolverMap';
import { GraphQLSchema } from 'graphql';

const schema: GraphQLSchema = makeExecutableSchema({
  typeDefs,
  resolvers,
});

export default schema;