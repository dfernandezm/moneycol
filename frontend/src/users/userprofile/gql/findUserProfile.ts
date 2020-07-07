import gql from 'graphql-tag';

export const FIND_USER_PROFILE_QUERY = gql`
    query findUserProfile($userId: String!) {
        findUserProfile(userId: $userId) {
        userId
        email
        username
        firstName
        lastName
        } 
     }`;