import React from 'react';
import './css/searchResult.css';

export default function SearchResultsMessage({ message = "Search results for", searchTerm }) {
  return (
        <p className="searchResultsMessage">{message}: {searchTerm}</p>
    )
}  
    
