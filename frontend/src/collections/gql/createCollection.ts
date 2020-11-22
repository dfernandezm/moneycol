import gql from 'graphql-tag';

export const CREATE_COLLECTION_MUTATION = gql`
  mutation createCollection($col: NewCollectionInput!) {
    addCollection(collection: $col) {
      collectionId
      name
      description
    }
  }
`