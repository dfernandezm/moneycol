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
            <div className="col s10 m10 card-container">
                { resultList.map((banknote, index) => {
                  console.log("Bank: ", banknote);
let a = <SearchResultItem item={banknote} index={index} key={banknote.CatalogCode} /> ;
return a;}) }  
            </div>
        </div>      
    </div> 
  )
}