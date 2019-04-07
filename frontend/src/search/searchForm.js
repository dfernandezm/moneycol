import React from 'react';

export default function SearchForm({ onChange, searchTerm, labelTitle = "Search"}) {
  return (
    <div className="row">
      <div className="input-field col s4 left">
          <input id="searchTerm" type="text" 
                  name="searchTerm"
                  onChange={onChange}
                  value={searchTerm} />
          <label htmlFor="searchTerm">{labelTitle}</label>
      </div> 
    </div> 
  )
}
