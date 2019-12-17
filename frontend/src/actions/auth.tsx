import { myFirebase } from "../firebase/firebase";
//import { Dispatch } from 'redux';
import {Action, ActionCreator, Dispatch} from 'redux';
import AuthenticationState from "../reducers"

export const LOGIN_REQUEST = "LOGIN_REQUEST";
export const LOGIN_SUCCESS = "LOGIN_SUCCESS";
export const LOGIN_FAILURE = "LOGIN_FAILURE";
export const LOGOUT_REQUEST = "LOGOUT_REQUEST";
export const LOGOUT_SUCCESS = "LOGOUT_SUCCESS";
export const LOGOUT_FAILURE = "LOGOUT_FAILURE";
export const VERIFY_REQUEST = "VERIFY_REQUEST";
export const VERIFY_SUCCESS = "VERIFY_SUCCESS";


export interface FirebaseUser {
  username: string,
  password?: string,
  email: string,
  firstname?: string,
  lastname?: string
}

export interface AuthenticationState {
  isLoggingIn: boolean,
  isLoggingOut: boolean,
  isVerifying: boolean,
  loginError: boolean,
  logoutError: boolean,
  isAuthenticated: boolean,
  verifyingError: boolean,
  user?: firebase.User | {}
}

interface RequestLoginAction {
  type: typeof LOGIN_REQUEST
}

interface ReceiveLoginAction {
  type: typeof LOGIN_SUCCESS
  user?: firebase.User
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

export type AuthenticationActionTypes = RequestLoginAction | ReceiveLoginAction | 
                                        RequestLogoutAction | ReceiveLoginFailureAction |
                                        RequestLogoutFailureAction | ReceiveLogoutAction |
                                        VerifyRequestAction | VerifySuccessAction;

const requestLogin: ActionCreator<RequestLoginAction> = () => {
  return {
    type: LOGIN_REQUEST
  };
};

const receiveLogin: ActionCreator<ReceiveLoginAction> = (user: firebase.User) => {
  return {
    type: LOGIN_SUCCESS,
    user
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

export const loginUser = (email: string, password: string) => 
  (dispatch: Dispatch<Action>) => {
    const a: RequestLoginAction  = requestLogin()
    dispatch(a);
    myFirebase
      .auth()
      .signInWithEmailAndPassword(email, password)
      .then((userCredential: firebase.auth.UserCredential) => {
        dispatch(receiveLogin(userCredential.user));
      })
      .catch(error => {
        //Do something with the error if you want!
        dispatch(loginError());
      });
  }

export const logoutUser = () => 
  (dispatch: Dispatch<RequestLogoutAction>) => {
    dispatch(requestLogout());
    myFirebase
      .auth()
      .signOut()
      .then(() => {
        dispatch(receiveLogout());
      })
      .catch(error => {
        //Do something with the error if you want!
        dispatch(logoutError());
      });
  };

export const verifyAuth = () => (dispatch: Dispatch) => {
  dispatch(verifyRequest());
  myFirebase.auth().onAuthStateChanged(user => {
    if (user !== null) {
      dispatch(receiveLogin(user));
    }
    dispatch(verifySuccess());
  });
};