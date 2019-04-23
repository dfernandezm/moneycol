import React from 'react';
import './css/searchBar.css';
export default function SearchBar({ onSubmit, onChange, searchTerm, placeholderText = "Search ..."}) {

    return (
        <form>
            <div class="input-field">
                <input id="search" type="search" placeholder={ placeholderText } />
                <label className="label-icon" htmlFor="search"><i class="material-icons">search</i></label>
                <i className="material-icons">close</i>
            </div>
        </form>
    );

}
    