import { LOGIN_GQL, GOOGLE_LOGIN_GQL } from '../gql/login';
import { LOGOUT_GQL } from '../gql/logout';
import { VERIFY_TOKEN_GQL } from '../gql/verifyToken';
import { Action, ActionCreator, Dispatch } from 'redux';
import { ApolloClient } from "apollo-boost";
import { localStateService } from "../localState/localStateService";

export const LOGIN_REQUEST = "LOGIN_REQUEST";
export const LOGIN_SUCCESS = "LOGIN_SUCCESS";
export const LOGIN_FAILURE = "LOGIN_FAILURE";
export const LOGOUT_REQUEST = "LOGOUT_REQUEST";
export const LOGOUT_SUCCESS = "LOGOUT_SUCCESS";
export const LOGOUT_FAILURE = "LOGOUT_FAILURE";
export const VERIFY_REQUEST = "VERIFY_REQUEST";
export const VERIFY_SUCCESS = "VERIFY_SUCCESS";

export interface AuthenticationState {
  isLoggingIn: boolean,
  isLoggingOut: boolean,
  isVerifying: boolean,
  loginError: boolean,
  logoutError: boolean,
  isAuthenticated: boolean,
  verifyingError: boolean,
  user?: any,
  token?: string | null
}

interface RequestLoginAction {
  type: typeof LOGIN_REQUEST
}

interface ReceiveLoginAction {
  type: typeof LOGIN_SUCCESS
  user?: any,
  token?: string | undefined
}

interface ReceiveLoginFailureAction {
  type: typeof LOGIN_FAILURE
}

interface RequestLogoutAction {
  type: typeof LOGOUT_REQUEST
}

interface RequestLogoutFailureAction {
  type: typeof LOGOUT_FAILURE
}

interface ReceiveLogoutAction {
  type: typeof LOGOUT_SUCCESS
}

interface VerifyRequestAction {
  type: typeof VERIFY_REQUEST
}

interface VerifySuccessAction {
  type: typeof VERIFY_SUCCESS
}

export type AuthenticationActionTypes =
  RequestLoginAction | ReceiveLoginAction |
  RequestLogoutAction | ReceiveLoginFailureAction |
  RequestLogoutFailureAction | ReceiveLogoutAction |
  VerifyRequestAction | VerifySuccessAction;

const requestLogin: ActionCreator<RequestLoginAction> = () => {
  return {
    type: LOGIN_REQUEST
  };
};

const receiveLogin: ActionCreator<ReceiveLoginAction> = (user: any, token: string) => {
  return {
    type: LOGIN_SUCCESS,
    user,
    token
  };
};

const loginError = () => {
  return {
    type: LOGIN_FAILURE
  };
};

const requestLogout: ActionCreator<RequestLogoutAction> = () => {
  return {
    type: LOGOUT_REQUEST
  };
};

const receiveLogout: ActionCreator<Action> = () => {
  return {
    type: LOGOUT_SUCCESS
  };
};

const logoutError: ActionCreator<Action> = () => {
  return {
    type: LOGOUT_FAILURE
  };
};

const verifyRequest = () => {
  return {
    type: VERIFY_REQUEST
  };
};

const verifySuccess = () => {
  return {
    type: VERIFY_SUCCESS
  };
};

// Redux thunk usage: https://redux.js.org/recipes/usage-with-typescript
// ApolloClient API reference: https://www.apollographql.com/docs/react/api/apollo-client/
export const loginUser = (email: string, password: string) =>
  async (dispatch: Dispatch<Action>, _: any, apolloClient: ApolloClient<any>) => {
    const requestLoginAction: RequestLoginAction = requestLogin()
    dispatch(requestLoginAction);

    try {
      const { data } = await apolloClient.mutate({
        mutation: LOGIN_GQL,
        variables: { email, password },
      });

      const token = data.loginWithEmail.token;
      const user = { userId: data.loginWithEmail.userId, email, token };

      if (token) {
        //FIXME: for security, token shouldn't be stored in localStorage
        localStateService.setToken(token);
        localStateService.setUserFromObject(user);
        console.log("Setting token", token);
        dispatch(receiveLogin(user, token));
        return "success";
      } else {
        console.log("Login error due to invalid or missing token");
        dispatch(loginError());
        return "error";
      }
    } catch (error) {
      console.log("Error logging in", error);
      dispatch(loginError());
      return "error";
    }
  }

  export const loginUserWithGoogle = (idToken: string) =>
  async (dispatch: Dispatch<Action>, _: any, apolloClient: ApolloClient<any>) => {
    const requestLoginAction: RequestLoginAction = requestLogin()
    dispatch(requestLoginAction);

    try {
      const googleAuthMaterial = { idToken };
      const { data } = await apolloClient.mutate({
        mutation: GOOGLE_LOGIN_GQL,
        variables: { googleAuthMaterial },
      });

      const { userId, token, email } = data.loginWithGoogle;
      const user = { userId, email, token };

      if (token) {
        //FIXME: for security, token shouldn't be stored in localStorage
        localStateService.setToken(token);
        localStateService.setUserFromObject(user);
        console.log("Setting token", token);
        dispatch(receiveLogin(user, token));
        return "success";
      } else {
        console.log("Login error due to invalid or missing token");
        dispatch(loginError());
        return "error";
      }
    } catch (error) {
      console.log("Error logging in", error);
      dispatch(loginError());
      return "error";
    }
  }


export const logoutUser = () =>
  async (dispatch: Dispatch<RequestLogoutAction>, _: any, apolloClient: ApolloClient<any>) => {
    dispatch(requestLogout());
    try {
      const currentToken = localStateService.getToken();
      if (!currentToken) {
        console.log("Already logged out as token is not present locally");
        dispatch(receiveLogout());
        return;
      }
      const { data } = await apolloClient.mutate({
        mutation: LOGOUT_GQL,
        variables: { token: currentToken }
      });

      if (data.logout.result && data.logout.result === "ok") {
        localStateService.clearToken();
        localStateService.clearUser();
        console.log("Removing token from local storage");
        dispatch(receiveLogout());
      } else {
        console.log("Error logging out", data);
        dispatch(logoutError());
      }
    } catch (err) {
      console.log("Error logging out", err);
      dispatch(logoutError());
    }
  };

export const verifyAuthWithDispatch = async (dispatch: Dispatch, _: any, apolloClient: ApolloClient<any>) => {
  dispatch(verifyRequest());

  // this invokes server for verification of the stored token
  // 1) If the token expired, it will try to reissue another if the user is logged in (long lived session)
  // 2) If the token has not expired and is valid, it will be refreshed
  // 3) If no token is stored or errors happen, login form will appear

  // To test the logged in scenarios, use the mutation to login or the login form once, then access from another 
  // tab or closed browser a protected route (use case for verify)
  
  try {
    let token = localStateService.getToken();
    let user = localStateService.getUser();

    // This can fail if userStr is undefined of empty catch block will pick it and prompt for login

    if (token && user) {
      const { data } = await apolloClient.mutate({
        mutation: VERIFY_TOKEN_GQL,
        variables: { token }
      });
      
      const newToken = data.verifyToken.token;
      let user = { email: data.verifyToken.email, userId: data.verifyToken.userId, token: newToken }
      
      localStateService.setToken(newToken);
      localStateService.setUserFromObject(user);

      dispatch(receiveLogin(user, newToken));
    } 
  } catch (err) {
    console.log("Error verifying, will need re-login", err);
  } finally {
    // We call verifySuccess to clear 'verifying' flag,
    // we should have verifyError action, but while we don't have
    // a server side verification in place this is ok
    dispatch(verifySuccess());
  }
};

export const verifyAuth = () => verifyAuthWithDispatch;

