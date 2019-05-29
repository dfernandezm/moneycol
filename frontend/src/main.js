import React from 'react';
import {
  Route,
  NavLink,
  BrowserRouter,
  Switch,
  withRouter
} from "react-router-dom";
import 'materialize-css/dist/css/materialize.min.css';
import SearchInTopBar from './search/searchInTopBar';
import SearchResultsPage from './search/searchResultsPage';
import Home from './home/home';
import User from './user/user';
import NavBar from './navbar/navBar';

export default class Main extends React.Component {
    render() {
      return (
        <BrowserRouter>
          
          <div className="mainpage">

            <NavBar />

            <div className="section no-pad-bot mainContent">
              <Switch>
                <Route exact={true} path="/" component={Home}/>
                <Route path="/user" component={withRouter(User)}/>  
                <Route path="/searchResultsPage" component={withRouter(SearchResultsPage)} />
              </Switch>
            </div>
          </div>

        </BrowserRouter>
      );
    }
  }
  