import React from 'react';
import {
  Route,
  NavLink,
  HashRouter
} from "react-router-dom";
import 'materialize-css/dist/css/materialize.min.css';
import Search from './search';
import Home from './home';

export default class MainMaterialize extends React.Component {
  
    render() {
      return (
        <HashRouter>
          <div className="mainpage">
            <nav>
              <div className="nav-wrapper">
              <NavLink exact={true} to="/" className="brand-logo">Swap Collections</NavLink>
                
                  <ul id="nav-mobile" className="right hide-on-med-and-down">
                    <li><NavLink exact={true} to="/"><i className="material-icons left">home</i>Home</NavLink></li>
                    <li><NavLink to="/search"><i className="material-icons left">search</i>Search</NavLink></li>
                    <li><NavLink to="/login"><i className="material-icons left">lock</i>Login</NavLink></li>
                  </ul>
              </div>
            </nav>
            <div className="section no-pad-bot mainContent">
              <Route exact={true} path="/" component={Home}/>
              <Route path="/search" component={Search}/> 
            </div>
          </div>
        </HashRouter>
      );
    }
  }
  