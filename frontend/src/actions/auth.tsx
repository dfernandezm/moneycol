import { myFirebase } from "../firebase/firebase";
import { Dispatch } from 'redux';

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
  user: firebase.User
}

const requestLogin = () => {
  return {
    type: LOGIN_REQUEST
  };
};

const receiveLogin = (user: firebase.User | null) => {
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

const requestLogout = () => {
  return {
    type: LOGOUT_REQUEST
  };
};


const receiveLogout = () => {
  return {
    type: LOGOUT_SUCCESS
  };
};

const logoutError = () => {
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

export const loginUser = (email: string, password: string) => (dispatch: Dispatch) => {
  dispatch(requestLogin());
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

export const logoutUser = () => (dispatch: Dispatch) => {
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