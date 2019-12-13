import React from 'react';
import {
  Route,
  BrowserRouter,
  Switch,
  withRouter
} from "react-router-dom";

import 'materialize-css/dist/css/materialize.min.css';
import SearchResultsPage from './search/searchResultsPage';
import Home from './home/home';
import { NavBar } from './navbar/navBar';

import { connect } from "react-redux";
import ProtectedRoute from "./login/ProtectedRoute";

import { Provider } from "react-redux";
//import App from "./App";
import configureStore from "./configureStore";
const store = configureStore();


const Main: React.FC = (props:any) => {
  const { isAuthenticated, isVerifying } = props;
  return (
    <Provider store={store}>
    <BrowserRouter>
      <NavBar />
      <div className="mainpage">
        <div className="section no-pad-bot mainContent">
          <Switch>
            <Route exact={true} path="/" component={Home} />
            <Route path="/searchResultsPage" component={withRouter(SearchResultsPage)} />
            <ProtectedRoute
              exact
              path="/protected"
              component={Home}
              isAuthenticated={isAuthenticated}
              isVerifying={isVerifying}
            />
          </Switch>
        </div>
      </div>

    </BrowserRouter>
    </Provider>
  );
}

function mapStateToProps(state) {
  return {
    isAuthenticated: state.auth.isAuthenticated,
    isVerifying: state.auth.isVerifying
  };
}

export default connect(mapStateToProps)(Main);