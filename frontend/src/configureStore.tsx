import { applyMiddleware, createStore, Store } from "redux";
import thunkMiddleware from "redux-thunk";
import { verifyAuthWithDispatch } from "./login/actions";
import rootReducer from "./login/reducers";

const configureStore = (persistedState = {}): Store => {

  const store: Store = createStore(
    rootReducer,
    persistedState,
    applyMiddleware(thunkMiddleware)
  );

  verifyAuthWithDispatch(store.dispatch)
  return store;
}

export default configureStore;