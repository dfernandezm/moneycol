import { applyMiddleware, createStore, Store } from "redux";
import thunkMiddleware from "redux-thunk";
import { verifyAuthWithDispatch } from "./login/actions";
import rootReducer from "./login/reducers";

// Pass in Apollo client: https://github.com/kriasoft/react-starter-kit/issues/1686
const configureStore = (apolloClient = {}, persistedState = {}): Store => {

  const store: Store = createStore(
    rootReducer,
    persistedState,
    applyMiddleware(thunkMiddleware.withExtraArgument(apolloClient))
  );

  verifyAuthWithDispatch(store.dispatch)
  
  return store;
}

export default configureStore;