import React from 'react';
import {
    Route,
    NavLink,
    HashRouter
  } from "react-router-dom";

//TODO: can be a function component (see gameInfo)
export default class SearchButton extends React.Component {
    render() {
        return (
            <li><NavLink href="/search"><i className="material-icons left">search</i>Search</NavLink></li>
        )
    }
}


