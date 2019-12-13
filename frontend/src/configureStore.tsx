import { applyMiddleware, createStore } from "redux";
import thunkMiddleware from "redux-thunk";
import { verifyAuth } from "./actions/";
import rootReducer from "./reducers";
import { auth } from "firebase";

export default function configureStore(persistedState = {}) {
    const store = createStore(
      rootReducer,
      persistedState,
      applyMiddleware(thunkMiddleware)
    );
    const authVerify: any = verifyAuth()
    store.dispatch(authVerify);
    return store;
  }