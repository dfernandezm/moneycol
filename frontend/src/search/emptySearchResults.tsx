import React from 'react';

const EmptySearchResults = ({ message = "No results to show" }) => {
  return (
    <div className="row">
      <div className="emptyResults">
        <h5>{message}</h5>
      </div>
    </div>
  )
}

export default EmptySearchResults;
