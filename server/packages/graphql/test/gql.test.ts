// const {createTestClient} = require('apollo-server-testing');
import { ApolloServer } from 'apollo-server';
import { createTestClient } from 'apollo-server-testing';
import gql from 'graphql-tag';
import { CollectionsRestDatasource } from '../src/infrastructure/collections/CollectionsRestDatasource';
import schema from '../src/schema';
const VALID_EXPIRED_TOKEN = "eyJhbGciOiJSUzI1NiIsImtpZCI6IjRlMDBlOGZlNWYyYzg4Y2YwYzcwNDRmMzA3ZjdlNzM5Nzg4ZTRmMWUiLCJ0eXAiOiJKV1QifQ.eyJuYW1lIjoiZGFmZSBEYWZlNTIiLCJpc3MiOiJodHRwczovL3NlY3VyZXRva2VuLmdvb2dsZS5jb20vbW9uZXljb2wiLCJhdWQiOiJtb25leWNvbCIsImF1dGhfdGltZSI6MTYxNjI3NDI1NiwidXNlcl9pZCI6IjEzTTk5UWFZbkNaMkFLa3dEbzRZeU1UdzFpaDEiLCJzdWIiOiIxM005OVFhWW5DWjJBS2t3RG80WXlNVHcxaWgxIiwiaWF0IjoxNjE2Mjc0MjU3LCJleHAiOjE2MTYyNzc4NTcsImVtYWlsIjoibW9uZXljb2x0ZXN0dXNlcjFAbWFpbGluYXRvci5jb20iLCJlbWFpbF92ZXJpZmllZCI6dHJ1ZSwiZmlyZWJhc2UiOnsiaWRlbnRpdGllcyI6eyJlbWFpbCI6WyJtb25leWNvbHRlc3R1c2VyMUBtYWlsaW5hdG9yLmNvbSJdfSwic2lnbl9pbl9wcm92aWRlciI6InBhc3N3b3JkIn19.cP6QTna7SHWSUFW5pAQDeBIrzVI5gr3No2d_p-qMpkfoFXt3TTcVUh8J0--1pfn8ydETiNzm_xv3pNUOtaIRN2QSoKK2RgyxNBcAgZFEohHZFlaumUUKOlh7RcP1w4_hYXkPQTg8fw1gb_a5RpljvKhuLtkpEYVimBJpb_LL34oJOlSaCOhxCmD_L126Vbb62lBXkIzepxhABwORmKS23QDLeXMBrDlImOMjHXyruGb1AXSDHbptiJxd8-ar7eaJT_ilgvWLgDCJCtMH-Qp69ml_1vhsgm-t8qszKhoTFnXYM_bVahPtMGCBzRd25DOxXam7VFUgY3FSdMa8JcyA8A";

describe('Mutations', () => {
  
    // https://github.com/apollographql/fullstack-tutorial/blob/master/final/server/src/__tests__/integration.js
    it('errors trying to create a collection without API', async () => {

        const collectionsAPI = new CollectionsRestDatasource();

        // create a test server to test against, using our production typeDefs,
        // resolvers, and dataSources.
        const server = new ApolloServer({
            schema,
            dataSources: () => ({ collectionsAPI }),
            context: () => ({ token: VALID_EXPIRED_TOKEN }),
        });

        const collectionInput = {
            name: "My collection",
            description: "This is my collection",
        }

        const { mutate } = createTestClient(server);
        const result = await mutate({
            mutation: CREATE_COLLECTION,
            variables: { collection: collectionInput },
        });

        expect(JSON.stringify(result)).toContain('Cannot read property \'response\' of undefined');
    });
});

export const CREATE_COLLECTION = gql`
  mutation addCollection($collection: NewCollectionInput!) {
    addCollection(collection: $collection) {
      collectionId
      name
      description
    }
  }
`;