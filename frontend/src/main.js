import React from 'react';
import SearchButton from './searchButton';
import MainContent from './mainContent';

export default class Main extends React.Component {
  
    render() {
      return (
        <div className="mainpage">
          <nav>
            <div class="nav-wrapper">
              <a href class="brand-logo">Swap Collection</a>
              <ul id="nav-mobile" class="right hide-on-med-and-down">
                <SearchButton />
                <li><a href="badges.html"><i className="material-icons left">lock</i>Login</a></li>
              </ul>
            </div>
          </nav>
          <div className="section no-pad-bot">
             <MainContent />
          </div>
        </div>
      );
    }
  }
  