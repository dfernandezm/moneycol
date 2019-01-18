import React from 'react';
import SomeCarousel from './carouselValid.js'; // don't use destructuring { Name } if only one export is done

export default class Main extends React.Component {
  
    render() {
      return (
        <div className="mainContent">
          <h3> Welcome </h3>
          <SomeCarousel />
        </div>
        
      );
    }
  }
  