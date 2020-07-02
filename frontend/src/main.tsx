import React from 'react';
import {
  Route,
  BrowserRouter,
  Switch,
  withRouter
} from "react-router-dom";

import SearchResultsPage from './search/searchResultsPage';
import Home from './home/home';

// import { NavBar } from './navbar/navBar';
import { NavBarMui }  from './navbar/navbarMui';

import { connect } from "react-redux";
import ProtectedRoute from "./login/protectedRoute";
import Protected  from "./login/protected";
import Login from "./login/login";
import { RootState } from './login/reducers';
import { CollectionsScreen } from './collections/collectionsScreen';
import VerifyEmail from './users/verifyEmail/verifyEmail';
import Signup from './users/signup/signup';
import UpdateUserProfile from './users/userprofile/updateUserProfile';

export interface MainProps {
  isAuthenticated: boolean,
  isVerifying: boolean
}

const Main: React.FC<MainProps> = (props: MainProps) => {

  const { isAuthenticated, isVerifying } = props;

  return ( 
    <BrowserRouter>
      <NavBarMui />
      {
        //TODO: put containers here to avoid style={{paddingTop: 64}}
      }
      <div className="mainpage" style={{paddingTop: 64}}>
        <div className="section no-pad-bot mainContent">
          <Switch>
            <Route exact={true} path="/" component={Home} />
            <Route path="/searchResultsPage" component={withRouter(SearchResultsPage)} />

            <ProtectedRoute
              exact
              path="/protected"
              component={Protected}
              isAuthenticated={isAuthenticated}
              isVerifying={isVerifying}
              />
            
            <ProtectedRoute
              exact
              path="/users/updateProfile"
              component={UpdateUserProfile}
              isAuthenticated={isAuthenticated}
              isVerifying={isVerifying}
              />
            
            <Route path="/login" component={Login} />
            <Route path="/collections" component={CollectionsScreen} />
            <Route path="/users/verifyEmail" component={VerifyEmail} />
            <Route path="/users/signup" component={Signup} />
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