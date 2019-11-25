import React, { useState, useEffect } from 'react';
import M from 'materialize-css';

import searchApi from '../apiCalls/searchApi';
import SearchBox from '../navbar/searchBox';
import RenderRedirect from './renderRedirect';

type SearchState = {
  searchTerm?: string,
  termUsed?: string,
  searchResults?: []
}

const SearchInTopBar: React.FC = () => {

  const [isTyping, setIsTyping] = useState(true);
  const [searchState, setSearchState] = useState<SearchState>({})

  useEffect(() => {
    M.updateTextFields();
  }, []) // react docs: pass empty array here to indicate that this does not depend on state or props (run only once)

  // termHasMinimumLength = () => {
  //   return this.state.searchTerm.length > 3;
  // }

  const shouldRenderResults = () => {
    // let isGoingToRender = !this.state.typing && this.termHasMinimumLength();
    return true;
  }

  // performSearchCall() {
  //   const searchTerm = this.state.searchTerm
  //   if (this.termHasMinimumLength()) {
  //     //TODO: sanitize search term before sending to server
  //     searchApi
  //       .searchApiCall(searchTerm, 0, 10)
  //       .then(resultData => {
  //         // with spread: same state but override typing with false, and searchResults becomes the current
  //         // 'searchResults' from API call (shortcut of {searchResults: searchResults})
  //         console.log("SearchRes ", resultData.results);
  //         this.setState({ ...this.state, typing: false, searchResults: resultData.results, searchTerm, termUsed: searchTerm }, () => {
  //           //TODO: this is here to avoid re-rendering 
  //           this.setState({ ...this.state, typing: true, searchTerm });
  //         });
  //       });
  //   }
  // }


  const onSubmit = (e: React.FormEvent) => {
    e.preventDefault();
    window.scrollTo(0, 0);
    //this.setState({ ...this.state, typing: false, searchResults: [] }, this.performSearchCall);
  }

  // //TODO: this could be optimized by only re-rendering the single input as it's typed on --
  // // right now it re-renders the whole search component (fully controlled)
  const updateSearchTerm = (event: React.FormEvent) => {
    // this.setState({
    //   ...this.state,
    //   typing: true,
    //   searchResults: [],
    //   searchTerm: event.target.value
    // });
  }

  const { termUsed, searchTerm, searchResults } = searchState;
  return (
    <>
      <SearchBox
        onSubmit={onSubmit}
        onChange={updateSearchTerm}
        searchTerm={searchTerm} />
      {shouldRenderResults() &&
        <RenderRedirect termUsed={termUsed} searchResults={searchResults} />
      }
    </>
  );
}

export default SearchInTopBar;