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
import ProtectedRoute from "./login/protectedRoute";
import Protected  from "./login/protected";
import Login from "./login/login";
import { RootState } from './login/reducers';

//TODO: where are these values coming from? the call to <Main /> is without props
export interface MainProps {
  isAuthenticated: boolean,
  isVerifying: boolean
}

const Main: React.FC<MainProps> = (props: MainProps) => {

  const { isAuthenticated, isVerifying } = props;

  return (
   
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
              component={Protected}
              isAuthenticated={isAuthenticated}
              isVerifying={isVerifying} />
            
            <Route path="/login" component={Login} />
          </Switch>
        </div>
      </div>

    </BrowserRouter>
  );
}

const mapState = (state: RootState) => {
  return {
    isAuthenticated: state.auth.isAuthenticated,
    isVerifying: state.auth.isVerifying
  };
}

// empty
const mapDispatch = {}

const connector = connect(
  mapState,
  mapDispatch
)

export default connector(Main);