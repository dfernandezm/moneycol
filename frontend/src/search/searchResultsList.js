import React from 'react';
import './css/searchResult.css';

import SearchResultItem from './searchResultItem';
import SearchResultsMessage from './searchResultsMessage';


// Cards list: https://codepen.io/jonvadillo/pen/PzYyEW
export default function SearchResultsList({ resultList = [], searchTerm = ""}) {
  return (
    <div className="row">
        <SearchResultsMessage searchTerm={searchTerm} />
        <div className="results">    
            <div className="col s12 m12 card-container">
                { resultList.map((banknote, index) => <SearchResultItem item={banknote} index={index} /> ) }  
            </div>
        </div>      
    </div> 
  )
}