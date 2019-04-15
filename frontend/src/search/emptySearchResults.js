import React from 'react';

export default function EmptySearchResults({message = "No results to show"}) {
  return (
    <div className="row">
      <div className="emptyResults">    
        <h5>{message}</h5>
      </div>      
    </div> 
  )
}
