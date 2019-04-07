import React from 'react';
import './css/search-icon.css';

export default function SearchFormInline({ onSubmit, onChange, searchTerm}) {
  return (
      <div className="searchForm">
        <form id="searchFormInline" onSubmit={onSubmit}>
          <div className="input-group">
                  <div className="input-field">
                    <input id="search" 
                      type="text"
                      name="searchTerm"
                      onChange={onChange}
                      value={searchTerm} />
                      <label className="label-icon" htmlFor="search">
                        <i className="material-icons prefix">search</i>
                      </label>
                  </div>
                <button type="submit" className="input-group-addon btn">search</button>
              </div>
        </form>
      </div>
  )
}
