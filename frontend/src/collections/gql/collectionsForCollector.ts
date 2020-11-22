import gql from 'graphql-tag';

export const COLLECTIONS_FOR_COLLECTOR = gql`
    query collectionsForCollector($collectorId: String!) {
      collectionsForCollector(collectorId: $collectorId) {
        collectionId
        name
        description
        bankNotes {
          country
          banknoteName
          year
        }
      }
    }
`