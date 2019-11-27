import React, { useState, useEffect } from 'react';
import M from 'materialize-css';

import SearchResultsList from './searchResultsList';
import EmptyResults from './emptySearchResults';
import searchApi from '../apiCalls/searchApi';
import InfiniteScroll from 'react-infinite-scroll-component';
import queryString from 'query-string';
import { SearchResult } from './types/SearchResult';
import { RouteProps, RouteComponentProps, withRouter } from 'react-router';

const termHasMinimumLength = (searchTerm: string) => {
  return searchTerm.length > 3;
}

//TODO: If we want bookmarks, this component should probably re-search if state is not present 
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

  const [resultsState, setResultsState] = useState<ResultsPageState>({ searchResults: [] })

  console.log("REDIRECT HERE: Result from the previous search (props)", props.location.state);
  console.log("REDIRECT HERE: RESULTSSTATE", resultsState.searchResults);

  const hasResultsToShow =
    props.location.state &&
    props.location.state.results &&
    props.location.state.results.length > 0;

  const shouldRenderResults = () => {
    let hasBeenRedirected = props.location !== undefined;
    let hasResultsToShow = props.location.state &&
      props.location.state.searchResults &&
      props.location.state.searchResults.length > 0;
    let stateHasResults = resultsState.searchResults && resultsState.searchResults.length > 0;
    console.log(">>>>>>>>>>>>> State has results <<<<<<<<<<<<<<<<", stateHasResults);
    return (hasBeenRedirected && hasResultsToShow) || stateHasResults;
  }



  const updateStateWith = (newResultData: SearchResultsData) => {
    console.log("Total length: " + newResultData.total);
    // const {fromOffset, searchResults} = resultsState;
    // const {total, results} = newResultData;

    // setResultsState({
    //   fromOffset: fromOffset + 10,
    //   totalResultLength: total,
    //   searchResults: searchResults.concat(results)
    // })
    // this.setState({
    //   ...this.state,
    //   from: this.state.from + 10,
    //   totalResultLength: newResultData.total,
    //   searchResults: this.state.searchResults.concat(newResultData.results)
    // });
  }

  // const performSearchCall = (searchTerm: string) => {
  //   console.log("New search call");
  //   if (termHasMinimumLength(searchTerm)) {
  //     searchApi
  //       .searchApiCall(searchTerm, resultsState.fromOffset || 0, 10)
  //       .then(resultData => {
  //         console.log("Data arrived", resultData);
  //         setResultsState({ ...resultsState, totalResultLength: resultData.total, searchResults: resultData.results });
  //       });
  //   }
  // }

  // We have to call 'searchFromUrl' here as well in case a direct link to /search?qs=term is invoked first time round
  useEffect(() => {
    console.log("==== SEARCH RESULTS USEEFFECT ====");
    M.updateTextFields();
    //console.log("Result from the previous search", resultsState);
    console.log("Result from the previous search (props)", props.location);
    const hasResultsToShow =
      props.location.state &&
      props.location.state.searchResults &&
      props.location.state.searchResults.length > 0;
    if (!hasResultsToShow) {
      console.log("No results to show");
      let qsTerm = searchTermFromQueryString(props.location.search);
      if (qsTerm) {
        console.log("Searching from queryString term: ", qsTerm);
        //performSearchCall(qsTerm);
      }
      //searchFromUrlTermIfFound();
    } else {
      // redirected from search, has state, set it in this component state and show them
      console.log("Has results to show");
      console.log("State: ", props.location.state);
      setResultsState({ searchResults: props.location.state.searchResults, searchTerm: props.location.state.searchTerm });
    }

  }, [props.location.state.searchTerm]);

  const fetchMoreData = () => {
    // if (this.state.searchResults.length >= this.state.totalResultLength) {
    //   console.log("No More data");
    //   this.setState({ hasMore: false });
    //   return;
    // }
    // performSearchCall();
  };

  return (
    <div className="searchResults">
      {resultsState.searchResults.length == 0 || !shouldRenderResults() ?
        <EmptyResults message="No results found" /> :
        <SearchResultsList
          resultList={resultsState.searchResults}
          searchTerm={props.location.search.replace("?qs=", "")} />
        // <InfiniteScroll
        //   dataLength={resultsState.searchResults.length}
        //   next={fetchMoreData}
        //   hasMore={resultsState.hasMore}
        //   height={600}
        //   loader={<h4>Loading...</h4>}>

        //   <SearchResultsList
        //     resultList={resultsState.searchResults}
        //     searchTerm={props.location.search.replace("?qs=", "")} />

        // </InfiniteScroll>
      }
    </div>
  );
}

export default SearchResultsPage;
//https://stackoverflow.com/questions/48219432/react-router-typescript-errors-on-withrouter-after-updating-version