import React from 'react';
import {
  Route,
  NavLink,
  HashRouter
} from "react-router-dom";
import 'materialize-css/dist/css/materialize.min.css';
import Search from './search';
import Home from './home';
import User from './user';

export default class MainMaterialize extends React.Component {
  //TODO: register / sign-in with Google
  //https://github.com/the-road-to-react-with-firebase/react-firebase-authentication/blob/2b28b831a7cd9b6ef5d4c5808a886ace159f3d2e/src/components/SignIn/index.js
    render() {
      return (
        <HashRouter>
          <div className="mainpage">
            <nav>
              <div className="nav-wrapper">
              <NavLink exact={true} to="/" className="brand-logo">Banknotes Collection</NavLink>
                
                  <ul id="nav-mobile" className="right hide-on-med-and-down">
                    <li><NavLink exact={true} to="/"><i className="material-icons left">home</i>Home</NavLink></li>
                    <li><NavLink to="/search"><i className="material-icons left">search</i>Search</NavLink></li>
                    
                    <li><NavLink to="/user"><i className="material-icons left">lock</i>Register</NavLink></li>
                  </ul>
              </div>
            </nav>
            <div className="section no-pad-bot mainContent">
              <Route exact={true} path="/" component={Home}/>
              <Route path="/search" component={Search}/>
              <Route path="/user" component={User}/>  
            </div>
          </div>
        </HashRouter>
      );
    }
  }
  