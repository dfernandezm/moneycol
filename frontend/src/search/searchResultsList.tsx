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
  return (
    <div className="row">
      <SearchResultsMessage searchTerm={searchTerm} />
      <div className="results">
        <div className="col s10 m10 card-container">
          {resultList.map((banknote, index) => {
            console.log("Bank: ", banknote);
            let a = <SearchResultItem item={banknote} index={index} key={banknote.CatalogCode} />;
            return a;
          })}
        </div>
      </div>
    </div>
  )
}

export default SearchResultsList;