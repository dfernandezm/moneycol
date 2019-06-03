import React from 'react';
import './css/searchResult.css';

export default function SearchResultsItem({ item, index }) {
  return (

        <div className="card horizontal" key={index}>
            <div className="card-image">
                <img src="https://i.colnect.net/b/2589/242/10-Dollars.jpg" alt="img"/>
            </div>
            <div className="card-stacked">
                <div className="card-content">
                    <h3>{item.Country} - {item.BanknoteName}</h3>
                    <h4>{item.Year}</h4>
                    <p>{item.Description}</p>
                </div>
                <div className="card-action">
                    <a href="#" className="green-link">Add to collection</a>
                </div>
            </div>
        </div>   
    )
}  
    
