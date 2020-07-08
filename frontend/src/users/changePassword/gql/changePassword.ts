import gql from 'graphql-tag';

export const CHANGE_PASSWORD_MUTATION = gql` 
mutation changePassword($changePasswordInput: ChangePasswordInput!) {
    changePassword(changePasswordInput: $changePasswordInput) {
      result
    }
  }`;

export const REQUEST_PASSWORD_RESET = gql`
mutation requestPasswordReset($email: String!) {
  requestPasswordReset(email: $email) {
    result
  }
}`;

export const COMPLETE_PASSWORD_RESET = gql`
mutation completePasswordReset($email: String!, $code: String!, $newPassword: String!){
  completePasswordReset(email: $email, code: $code, newPassword: $newPassword) {
    result
  }
}
`;