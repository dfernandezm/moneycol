import gql from 'graphql-tag';

export const VERIFY_EMAIL_GQL = gql`
    mutation verifyEmail($verifyEmailInput: VerifyEmailInput!) {
        verifyEmail(verifyEmailInput: $verifyEmailInput) {
            comebackUrl
            result
        }
    }
`;