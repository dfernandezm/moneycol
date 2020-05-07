import React from 'react';
import {
  Route,
  BrowserRouter,
  Switch,
  withRouter
} from "react-router-dom";

import SearchResultsPage from './search/searchResultsPage';
import Home from './home/home';
import { NavBarMui }  from './navbar/navbarMui';

import ProtectedRoute from "./login/protectedRoute";
//import Protected  from "./login/protected";
import ProtectedGql  from "./login/protectedGql";
import LoginGql from "./login/loginGql";
import { CollectionsScreen } from './collections/collectionsScreen';

export interface MainProps {
  isAuthenticated: boolean
//  isVerifying: boolean
}

const MainGql: React.FC<MainProps> = (props: MainProps) => {

  const { isAuthenticated } = props;

  //TODO: put containers here to avoid style={{paddingTop: 64}}

  return ( 
    <BrowserRouter>
      <NavBarMui />
      <div className="mainpage" style={{paddingTop: 64}}>
        <div className="section no-pad-bot mainContent">
          <Switch>
            <Route exact={true} path="/" component={Home} />
            <Route path="/searchResultsPage" component={withRouter(SearchResultsPage)} />

            <ProtectedRoute
              exact
              path="/protected"
              component={ProtectedGql}
              isAuthenticated={isAuthenticated}
              isVerifying={false} //TODO: add back
              />
            
            <Route path="/login" component={LoginGql} />
            {/* <Route path="/collections" component={CollectionsScreen} /> */}
            <ProtectedRoute
              exact
              path="/collections"
              component={CollectionsScreen}
              isAuthenticated={isAuthenticated}
              isVerifying={false} //TODO: add back
              />
          </Switch>
        </div>
      </div>

    </BrowserRouter>
  );
}

// const mapState = (state: RootState) => {
//   return {
//     isAuthenticated: state.auth.isAuthenticated,
//     isVerifying: state.auth.isVerifying
//   };
// }

// empty
// const mapDispatch = {}

// const connector = connect(
//   mapState,
//   mapDispatch
// )

export default MainGql;