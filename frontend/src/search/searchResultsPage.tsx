import React, { useEffect, useState } from 'react';

import EmptyResults from './emptySearchResults';
import RenderRedirect from './redirectToResultsPage';
import { RouteComponentProps, StaticContext } from 'react-router';
import { SearchResult } from './types/SearchResult';
import SearchResultsList from './searchResultsList';
import queryString from 'query-string';

//import { useApolloClient } from '@apollo/react-hooks';

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

//type Props = RouteComponentProps<{}, StaticContext, { searchTerm: string, searchResults: Array<any> }>;

type Props = RouteComponentProps<{}, StaticContext, ResultsPageState>;

const searchTermFromQueryString = (searchLocation: string) => {
  const queryStringValues = queryString.parse(searchLocation);
  const qs = queryStringValues.qs;
  return qs as string;
}

const SearchResultsPage: React.FC<Props> = (props: Props) => {

  const [newSearchTerm, setNewSearchTerm] = useState("");
  const resultsValues = props.location.state;

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

        (resultsValues.searchResults.length === 0 || !shouldRenderResults() ?
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
