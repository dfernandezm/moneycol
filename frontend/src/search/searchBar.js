import React from 'react';
import './css/searchBar.css';
export default function SearchBar({ onSubmit, onChange, searchTerm, placeholderText = "Search ..."}) {

    return (
        <form onSubmit={onSubmit}>
            <div className="input-field">
                <input id="search"
                 type="search"
                 name="searchTerm"
                 onChange={onChange}
                 value={searchTerm} 
                 placeholder={ placeholderText } />
                <label className="label-icon" htmlFor="search"><i className="material-icons">search</i></label>
                <i className="material-icons">close</i>
            </div>
        </form>
    );

}
    