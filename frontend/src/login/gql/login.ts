import gql from 'graphql-tag';

export const LOGIN_GQL = gql`
    mutation login($email: String!, $password: String!) {
        loginWithEmail(email: $email, password: $password) {
            token
            email
            userId
        }
    }
`;

export const GOOGLE_LOGIN_GQL = gql`
   mutation loginWithGoogle($googleAuthMaterial: GoogleAuthMaterial!) {
        loginWithGoogle(googleAuthMaterial: $googleAuthMaterial) {
            email
            userId
            token
        }
    }
`;