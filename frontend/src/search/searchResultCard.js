import React from 'react';
import './css/searchResult.css';

export default function SearchResultsList({ resultList = []}) {
  return (
    <div className="row">
      <div className="results">    
        <div className="col s12 m12 card-container">
            {resultList.map(banknote =>
                <div className="card" key={banknote.CatalogCode}>
                    <div className="card-image">
                        <img alt="img" src="https://i.colnect.net/f/4565/935/1-Dollar.jpg" />
                    </div>
                    <div className="card-content">
                        <span className="card-title">{banknote.Country} - {banknote.BanknoteName} ({banknote.Year})</span>
                        <p>{banknote.Description}</p>
                    </div>
                    <div className="card-action">
                        <a href="#">Add to collection</a>
                    </div>
                </div>
            )}  
        </div>
    </div>      
  </div> 
  )
}