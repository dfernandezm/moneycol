import gql from 'graphql-tag';

export const SIGNUP_USER_MUTATION = gql`
    mutation signUp($userInput: SignUpUserInput!) {
        signUpWithEmail(userInput: $userInput) {
            userId
            username
            email
            firstName
            lastName  	
        }
    }
`;