import React, { useState, useEffect } from 'react';
import M from 'materialize-css';

import searchApi from '../apiCalls/searchApi';
import SearchBox from '../navbar/searchBox';
import RenderRedirect from './renderRedirect';
import { SearchResult } from './types/SearchResult';

const termHasMinimumLength = (searchTerm: string) => {
  return searchTerm.length > 3;
}

const SearchInTopBar: React.FC = () => {

  const [isTyping, setIsTyping] = useState(true);
  const [submittingSearch, setSubmittingSearch] = useState(false);
  const [searchTerm, setSearchTerm] = useState("");
  const [searchResults, setSearchResults] = useState<SearchResult[]>([]);
  const [redirectToResults, setRedirectToResults] = useState(false);

  useEffect(() => {
    M.updateTextFields();
  }, []) // react docs: pass empty array here to indicate that this does not depend on state or props (run only once)

  useEffect(() => {
    if (submittingSearch) {
      performSearchCall();
    }
  }, [submittingSearch])

  const shouldRenderResults = () => {
    let isGoingToRender = !isTyping && termHasMinimumLength(searchTerm);
    return isGoingToRender;
  }

  const performSearchCall = () => {
    //TODO: error handling

    if (termHasMinimumLength(searchTerm)) {
      //TODO: sanitize search term before sending to server
      searchApi
        .searchApiCall(searchTerm, 0, 10)
        .then(resultData => {
          setSubmittingSearch(false)
          // with spread: same state but override typing with false, and searchResults becomes the current
          // 'searchResults' from API call (shortcut of {searchResults: searchResults})
          console.log("SearchRes ", resultData.results[0]);
          setSearchResults(resultData.results);
          setIsTyping(true);
          setRedirectToResults(true)

          //setSearchState({ ...searchState, searchResults: resultData.searchResults, searchTerm, termUsed: searchTerm });

          // this.setState({ ...this.state, typing: false, searchResults: resultData.results, searchTerm, termUsed: searchTerm }, () => {
          //   //TODO: this is here to avoid re-rendering 
          //   this.setState({ ...this.state, typing: true, searchTerm });
          // });
        });
    }
  }

  const onSubmit = (e: React.FormEvent) => {
    e.preventDefault();
    window.scrollTo(0, 0);
    setIsTyping(false);
    setSubmittingSearch(true);
    //this.setState({ ...this.state, typing: false, searchResults: [] }, this.performSearchCall);
  }

  // //TODO: this could be optimized by only re-rendering the single input as it's typed on --
  // // right now it re-renders the whole search component (fully controlled)
  const updateSearchTerm = (e: React.FormEvent) => {
    const target = e.target as HTMLInputElement;
    setRedirectToResults(false);
    setIsTyping(true);
    setSearchResults([]);
    setSearchTerm(target.value);
    //setSearchState({ ...setSearchState, searchResults: [], searchTerm: target.value });
    // this.setState({
    //   ...this.state,
    //   typing: true,
    //   searchResults: [],
    //   searchTerm: event.target.value
    // });
  }

  return (
    <>
      <SearchBox
        onSubmit={onSubmit}
        onChange={updateSearchTerm}
        searchTerm={searchTerm} />
      {redirectToResults &&
        <RenderRedirect termUsed={searchTerm} searchResults={searchResults} />
      }
    </>
  );
}

export default SearchInTopBar;