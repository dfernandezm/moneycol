import React from 'react';

export default function SearchForm({ updateInputOnChange, searchTerm, labelTitle = "Search"}) {
  return (
    <div className="row">
      <div className="input-field col s4 left">
          <input id="searchTerm" type="text" 
                  name="searchTerm"
                  onChange={updateInputOnChange}
                  value={searchTerm} />
          <label htmlFor="searchTerm">{labelTitle}</label>
      </div> 
    </div> 
  )
}
