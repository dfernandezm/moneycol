import gql from 'graphql-tag';

export const LOGOUT_GQL = gql`
  mutation logout($token: String!) {
    logout(token: $token) {
      result
    }
  }
`;