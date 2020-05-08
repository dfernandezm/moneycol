import { LOGIN_GQL } from '../gql/login';
import { LOGOUT_GQL } from '../gql/logout';
import { Action, ActionCreator, Dispatch } from 'redux';
import { ApolloClient } from "apollo-boost";

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
  user?: firebase.User,
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
        localStorage.setItem("token", token);
        localStorage.setItem("user", JSON.stringify(user));
        console.log("Setting token in local storage:", token);
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
      const { data } = await apolloClient.mutate({
        mutation: LOGOUT_GQL,
      });

      if (data.logout.result && data.logout.result === "ok") {
        localStorage.removeItem("token");
        localStorage.removeItem("user");
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

export const verifyAuthWithDispatch = (dispatch: Dispatch) => {
  dispatch(verifyRequest());

  //TODO: should call verify mutation: existing token/verify with firebaseCurrentUser mutation (#177)
  // firebase.auth().onAuthStateChanged(async (user: firebase.User) 
  try {
    const token = localStorage.getItem("token");
    const userStr = localStorage.getItem("user");
    const user = userStr ? JSON.parse(userStr) : null;
    if (token && user) {
      dispatch(receiveLogin(user, token));
    } 
  } catch (err) {
    console.log("Error verifying", err);
  } finally {
    // We call verifySuccess to clear 'verifying' flag,
    // we should have verifyError action, but while we don't have
    // a server side verification in place this is ok
    dispatch(verifySuccess());
  }
};

export const verifyAuth = () => verifyAuthWithDispatch;

