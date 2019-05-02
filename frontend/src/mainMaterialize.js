import React from 'react';
import {
  Route,
  NavLink,
  BrowserRouter,
  Switch,
  withRouter
} from "react-router-dom";
import 'materialize-css/dist/css/materialize.min.css';
import Search from './search/search';
import SearchInTopBar from './search/searchInTopBar';
import SearchResultsPage from './search/searchResultsPage';
import Home from './home';
import User from './user';

export default class MainMaterialize extends React.Component {
  //TODO: register / sign-in with Google
  //https://github.com/the-road-to-react-with-firebase/react-firebase-authentication/blob/2b28b831a7cd9b6ef5d4c5808a886ace159f3d2e/src/components/SignIn/index.js
    render() {
      return (
        <BrowserRouter>
          <div className="mainpage">
            <nav>
              <div className="nav-wrapper">
               <div className="row">
                <div className="col s4 left">
                  <NavLink exact={true} to="/" className="brand-logo">Banknotes Collection</NavLink>
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
                <Route path="/search" component={withRouter(Search)}/>
                <Route path="/user" component={Home}/>  
                <Route path="/searchResultsPage" component={withRouter(SearchResultsPage)} />
              </Switch>
            </div>
          </div>
        </BrowserRouter>
      );
    }
  }
  