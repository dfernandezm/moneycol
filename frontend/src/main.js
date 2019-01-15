import React from 'react';
import {Navbar, NavItem} from 'react-materialize'

export default class Main extends React.Component {
  
    render() {
      return (
        <Navbar brand='logo' right>
          <NavItem onClick={() => console.log('test click')}>Getting started</NavItem>
          <NavItem href='components.html'>Components</NavItem>
        </Navbar>
      );
    }
  }
  