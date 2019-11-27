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

const Main: React.FC = (props) => {
  return (
    <BrowserRouter>
      <NavBar />
      <div className="mainpage">
        <div className="section no-pad-bot mainContent">
          <Switch>
            <Route exact={true} path="/" component={Home} />
            <Route path="/searchResultsPage" component={withRouter(SearchResultsPage)} />
          </Switch>
        </div>
      </div>

    </BrowserRouter>
  );
}

export { Main };