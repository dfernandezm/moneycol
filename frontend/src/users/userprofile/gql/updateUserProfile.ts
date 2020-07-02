import gql from 'graphql-tag';

export const UPDATE_USER_PROFILE_MUTATION = gql`
   mutation updateUserProfile($userInput: UpdateUserProfileInput!) {
    updateUserProfile(updateUserProfileInput: $userInput) {
        userId,
        username,
        firstName,
        lastName
    }
}
`;