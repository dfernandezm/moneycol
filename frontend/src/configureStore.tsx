import { applyMiddleware, createStore, Store } from "redux";
import thunkMiddleware from "redux-thunk";
import { verifyAuthWithDispatch } from "./actions/";
import rootReducer from "./reducers";

const configureStore = (persistedState = {}) => {

  const store: Store = createStore(
    rootReducer,
    persistedState,
    applyMiddleware(thunkMiddleware)
  );

  // const authVerify: any = verifyAuth();
  verifyAuthWithDispatch(store.dispatch)
  return store;
}

export default configureStore;