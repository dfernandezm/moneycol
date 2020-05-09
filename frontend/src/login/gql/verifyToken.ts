import gql from 'graphql-tag';

export const VERIFY_TOKEN_GQL = gql`
   mutation verifyToken($token: String!) {
        verifyToken(token: $token, refresh: true) {
            userId
            email
            token
        }
    }
`;