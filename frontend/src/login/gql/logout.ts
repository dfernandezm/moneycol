import gql from 'graphql-tag';

export const LOGOUT_GQL = gql`
    mutation logout {
        logout {
            result
        }
    }
`;