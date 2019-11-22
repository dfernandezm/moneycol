import React, { useLayoutEffect, useState, useEffect } from 'react';
import {
    NavLink
} from "react-router-dom";

import SearchBox from './searchBox';
import './navBar.css';
import logo from './moneycol-logo.png';
import SearchInTopBar from '../search/searchInTopBar';

export const NavBar: React.FC = () => {

    const [measures, setMeasures] = useState({ top: 0, height: 0, scroll: -1 });
    const [initLoad, setInitLoad] = useState(true);

    useEffect(() => {
        if (initLoad) {
            const el = document.querySelector('nav');
            setMeasures({ top: el.offsetTop, height: el.offsetHeight, scroll: -1 });
            setInitLoad(false);
            window.addEventListener('scroll', this.handleScroll);
        } else {
            measures.scroll > measures.top ?
                document.body.style.paddingTop = `${this.state.height}px` :
                document.body.style.paddingTop = '0px';
        }
    });

    const handleScroll = () => {
        setMeasures({ ...measures, scroll: window.scrollY });
    }
    
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
