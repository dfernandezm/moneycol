import React from 'react';
import {
  NavLink
} from "react-router-dom";

import SearchBox from './searchBox';
import './navBar.css';
import logo from './moneycol-logo.png';
import SearchInTopBar from '../search/searchInTopBar';

export default class NavBar extends React.Component {
    constructor(props) {
        super(props)
        this.state = {};
        this.handleScroll = this.handleScroll.bind(this);
    }

    handleScroll() {
        this.setState({scroll: window.scrollY});
    }

    componentDidMount() {
        const el = document.querySelector('nav');
        this.setState({top: el.offsetTop, height: el.offsetHeight});
        window.addEventListener('scroll', this.handleScroll);
    }
  
    componentDidUpdate() {
        this.state.scroll > this.state.top ? 
            document.body.style.paddingTop = `${this.state.height}px` :
            document.body.style.paddingTop = 0;
    }



    render() {
        return (
            <nav className={this.state.scroll > this.state.top ? "fixed-nav" : ""}>
                <div className="nav-wrapper">
                    <div className="row">
                        <div className="col left">
                            <div className="nav__logo">
                                <NavLink exact={true} to="/" ><img src={logo} alt="logo" /></NavLink>
                            </div>
                        </div>
                        <div className="col s5 left">
                            <NavLink exact={true} to="/" className="brand-name">MoneyCol</NavLink>
                        </div>

                        <div className="col s3">
                            <SearchInTopBar />
                       </div>
                        <div className="col s3 right">
                            <ul id="nav-mobile" className="right hide-on-med-and-down nav__menu--right">
                                <li><NavLink exact={true} to="/">My Collections</NavLink></li>
                                {/* <li><NavLink to="/user"><i className="material-icons left nav__material-icon-link">account_circle</i></NavLink></li> */}
                                <li><NavLink to="/user">Sign in</NavLink></li>
                            </ul>
                        </div>
                        </div>
                       
                    </div>
            </nav>
        );
    }

}
