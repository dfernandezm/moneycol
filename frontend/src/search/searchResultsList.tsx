import React from 'react';
import './css/searchResult.css';

import SearchResultItem from './searchResultItem';
import SearchResultsMessage from './searchResultsMessage';
import { SearchResult } from './types/SearchResult';

type SearchResultsListProps = {
  resultList: SearchResult[],
  searchTerm: string
}

// Cards list: https://codepen.io/jonvadillo/pen/PzYyEW
const SearchResultsList: React.FC<SearchResultsListProps> = ({ resultList = [], searchTerm = "" }) => {
  console.log("ResultsList:", resultList);
  return (
    <div className="row">
      <SearchResultsMessage searchTerm={searchTerm} />
      <div className="results">
        <div className="col s10 m10 card-container">
          {resultList.map((banknote, index) => {
            console.log("Banknote: ", banknote);
            return <SearchResultItem item={banknote} index={index} key={banknote.catalogCode} />;
          })}
        </div>
      </div>
    </div>
  )
}

export default SearchResultsList;