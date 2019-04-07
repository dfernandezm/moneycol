import React from 'react';

export default function SearchResultsList({ resultList = []}) {
  return (
    <div className="row">
      <div className="results">    
        Results: <br />
        <ul>
          {resultList.map(banknote =>
          <li key={banknote.CatalogCode}>
            {banknote.Country}: {banknote.BanknoteName} ({banknote.Year})
          </li>
        )}
      </ul>
    </div>      
  </div> 
  )
}
