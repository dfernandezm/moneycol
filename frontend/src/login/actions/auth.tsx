import { myFirebase } from "../../firebase/firebase";
import { Action, ActionCreator, Dispatch } from 'redux';

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
  user?: firebase.User | {},
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

const receiveLogin: ActionCreator<ReceiveLoginAction> = (user: firebase.User, token: string) => {
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

export const loginUser = (email: string, password: string) =>
  (dispatch: Dispatch<Action>) => {
    const requestLoginAction: RequestLoginAction = requestLogin()
    dispatch(requestLoginAction);

    myFirebase()
    .then(firebase => 
      firebase
        .auth()
        .signInWithEmailAndPassword(email, password)
        .then(async (userCredential: firebase.auth.UserCredential) => {
          const token = await tokenFromUser(userCredential.user);
          if (token) {
            //FIXME: for security, token shouldn't be stored in localStorage
            localStorage.setItem("token", token);
            dispatch(receiveLogin(userCredential.user, token));
          } else {
            console.log("Login error due to invalid or missing token");
            dispatch(loginError());
          }
        })
        .catch(() => {
          // Do something with the error
          dispatch(loginError());
        })
    );
  }

const tokenFromUser = async (user: firebase.User | null) => {
  if (user) {
    try {
      // flag for forceRefresh
      const idToken = await user.getIdToken(true);
      return idToken;
    } catch (error) {
      console.log("Error retrieving token: ", error);
      return null;
    }
  }
}

export const logoutUser = () =>
  (dispatch: Dispatch<RequestLogoutAction>) => {
    dispatch(requestLogout());
    myFirebase()
      .then(firebase => 
        firebase.auth().signOut()
        .then(() => {
          localStorage.removeItem("token");
          dispatch(receiveLogout());
        }).catch(() => {
          dispatch(logoutError());
        })
      ); 
  };

export const verifyAuthWithDispatch = (dispatch: Dispatch) => {
  dispatch(verifyRequest());
  myFirebase()
      .then(firebase => {
        firebase.auth().onAuthStateChanged(async (user: firebase.User) => {
          if (user !== null) {
            const token = await tokenFromUser(user);
            if (token) {
              localStorage.setItem("token", token);
              dispatch(receiveLogin(user, token));
            } else {
              console.log("Token is missing, cannot re-login");
            }
          }
          dispatch(verifySuccess());
        })
      });
};

export const verifyAuth = () => verifyAuthWithDispatch;

