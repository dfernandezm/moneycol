import React from 'react';
import {
  Route,
  BrowserRouter,
  Switch,
  withRouter
} from "react-router-dom";

import 'materialize-css/dist/css/materialize.min.css';
import { SEARCH_GQL } from './search/gql/search';
import SearchResultsPage from './search/searchResultsPage';
import Home from './home/home';
import { NavBar } from './navbar/navBar';
import { useQuery } from '@apollo/react-hooks';

const Main: React.FC = (props) => {
  // const { data, loading, error } = useQuery(SEARCH_GQL, {
  //   variables: { searchTerm: 'ireland' },
  // })
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