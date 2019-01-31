import React from 'react';
import SomeCarousel from './carouselValid.js'; // don't use destructuring { Name } if only one export is done

export default class Home extends React.Component {
  
    render() {
      return (
        <div className="home">
          <h3> Welcome </h3>
          <SomeCarousel />
        </div>
      );
    }
  }
  