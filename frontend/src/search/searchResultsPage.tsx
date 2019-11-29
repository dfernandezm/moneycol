import React, { useState, useEffect } from 'react';
import M from 'materialize-css';

import SearchResultsList from './searchResultsList';
import EmptyResults from './emptySearchResults';
import queryString from 'query-string';
import { SearchResult } from './types/SearchResult';
import { RouteComponentProps } from 'react-router';
import { useApolloClient } from '@apollo/react-hooks';
import RenderRedirect from './redirectToResultsPage';

// If we want bookmarks, this component should re-search if state from redirect is not present 
// and the url contains the search term

type ResultsPageState = {
  searchResults: SearchResult[]
  searchTerm?: string
  termUsed?: string
  totalResultLength?: number,
  hasMore?: boolean,
  fromOffset?: number
}

type SearchResultsData = {
  total?: number,
  results?: SearchResult[]
}

const style = {
  height: 30,
  border: "1px solid green",
  margin: 6,
  padding: 8
};

const searchTermFromQueryString = (searchLocation: string) => {
  const queryStringValues = queryString.parse(searchLocation);
  const qs = queryStringValues.qs;
  return qs as string;
}

const SearchResultsPage: React.FC<RouteComponentProps> = (props: RouteComponentProps) => {

  const [newSearchTerm, setNewSearchTerm] = useState("");
  const resultsValues = props.location.state;
  const client = useApolloClient();

  //console.log("REDIRECT HERE: Result from the previous search (props)", props.location.state);
  //console.log("REDIRECT HERE: RESULTS-STATE", resultsState.searchResults);

  const shouldRenderResults = () => {
    let hasBeenRedirected = props.location !== undefined;
    let hasResultsToShow = props.location.state &&
      props.location.state.searchResults &&
      props.location.state.searchResults.length > 0;
    return (hasBeenRedirected && hasResultsToShow);
  }

  // When the qs url parameter changes, we want to re-render and redirect to results
  useEffect(() => {
    const searchTerm = searchTermFromQueryString(props.location.search);
    setNewSearchTerm(searchTerm);
  }, [props.location.search]);

  return (
    <div className="searchResults">
      {resultsValues &&

        (resultsValues.searchResults.length == 0 || !shouldRenderResults() ?
          <EmptyResults message="No results found" /> :
          <SearchResultsList
            resultList={resultsValues.searchResults}
            searchTerm={props.location.search.replace("?qs=", "")} />)
      }
      {
        newSearchTerm && <RenderRedirect termUsed={newSearchTerm} />
      }
    </div>
  );
}

export default SearchResultsPage;
//https://stackoverflow.com/questions/48219432/react-router-typescript-errors-on-withrouter-after-updating-version