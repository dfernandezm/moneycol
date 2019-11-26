import gql from 'graphql-tag';

export const SEARCH_GQL = gql`
query searchBanknotes($searchTerm: String!) {
  search(term: $searchTerm) {
    results {
      ...BanknoteSearchResult
    }
    total
  }
}

fragment BanknoteSearchResult on BankNote {
  country
  banknoteName
  year
  description
  catalogCode
}
`;