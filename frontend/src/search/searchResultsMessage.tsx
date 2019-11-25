import React from 'react';
import './css/searchResult.css';

type SearchResultsMessage = {
  message?: string,
  searchTerm: string
}

const SearchResultsMessage: React.FC<SearchResultsMessage> = ({ message = "Search results for", searchTerm }) => {
  return (
    <p className="searchResultsMessage">{message}: {searchTerm}</p>
  )
}

export default SearchResultsMessage;

