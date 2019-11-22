import React from 'react';
import './searchBox.css';

type SearchBoxProps = {
    onSubmit, onChange, searchTerm, placeholderText
}

export const SearchBox: React.FC<SearchBoxProps> = 
    ({ onSubmit, onChange, searchTerm, placeholderText = "Search the catalog"}) => {

        return (
            <div className="searchBox__">
                <form onSubmit={onSubmit}>
                    <div className="searchBox__input input-field">
                        <input id="search"
                        type="search"
                        name="searchTerm"
                        onChange={onChange}
                        value={searchTerm} 
                        placeholder={ placeholderText } />
                        <label 
                            className="label-icon active" 
                            htmlFor="search">
                                <i className="material-icons">search</i>
                        </label>
                    </div>
                </form>
            </div>
        );
}
    