import React from 'react';

import Container from '@material-ui/core/Container';

import SearchResultItem from './searchResultItem';
import SearchResultsMessage from './searchResultsMessage';
import { SearchResult } from './types/SearchResult';

type SearchResultsListProps = {
  resultList: SearchResult[],
  searchTerm: string
}

const SearchResultsList: React.FC<SearchResultsListProps> = ({ resultList = [], searchTerm = "" }) => {
  return (
    <Container maxWidth="lg">
       <SearchResultsMessage searchTerm={searchTerm} />
       <Container maxWidth="md">
            {resultList.map((banknote, index) => {
              return <SearchResultItem item={banknote} index={index} key={banknote.catalogCode} />;
            })}  
        </Container>
    </Container>
  )
}

export default SearchResultsList;