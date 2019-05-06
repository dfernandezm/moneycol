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

export default class Main extends React.Component {
    render() {
      return (
        <BrowserRouter>
          <div className="mainpage">
            <nav>
              <div className="nav-wrapper">
               <div className="row">
                <div className="col s4 left">
                  <NavLink exact={true} to="/" className="brand-logo">Collections</NavLink>
                </div>
                <ul id="nav-mobile" className="right hide-on-med-and-down">
                    <li>
                      <SearchInTopBar /> 
                    </li>
                    <li><NavLink exact={true} to="/"><i className="material-icons left">home</i></NavLink></li>
                    <li><NavLink to="/search"><i className="material-icons left">search</i></NavLink></li>
                    <li><NavLink to="/user"><i className="material-icons left">account_circle</i></NavLink></li>
                </ul>
              </div>
              </div>
            </nav>

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
  