import React, { useState, useEffect } from 'react';
import { useQuery, useApolloClient } from '@apollo/react-hooks';
import M from 'materialize-css';

import searchApi from '../apiCalls/searchApi';
import SearchBox from '../navbar/searchBox';
import RenderRedirect from './renderRedirect';
import { SearchResult } from './types/SearchResult';

import { SEARCH_GQL } from './gql/search';

const termHasMinimumLength = (searchTerm: string) => {
  return searchTerm.length > 3;
}

const SearchInTopBar: React.FC = () => {

  const [isTyping, setIsTyping] = useState(true);
  const [submittingSearch, setSubmittingSearch] = useState(false);
  const [searchTerm, setSearchTerm] = useState('');
  const [usableSearchTerm, setUsableSearchTerm] = useState('');
  const [searchResults, setSearchResults] = useState<SearchResult[]>([]);
  const [redirectToResults, setRedirectToResults] = useState(false);
  const client = useApolloClient();

  // const { data, loading, error } = useQuery(SEARCH_GQL, {
  //   variables: { searchTerm: usableSearchTerm },
  // })

  useEffect(() => {
    M.updateTextFields();
  }, [])
  // react docs: pass empty array here to indicate that this does not depend on state or props (run only once)

  useEffect(() => {
    if (submittingSearch) {
      performSearchCall();
    }
  }, [submittingSearch])

  useEffect(() => {
    if (searchTerm.length > 2) {
      console.log("Usable search term: ", searchTerm);
      setUsableSearchTerm(searchTerm);
    }
  }, [searchTerm])

  const shouldRenderResults = () => {
    let isGoingToRender = !isTyping && termHasMinimumLength(searchTerm);
    return isGoingToRender;
  }

  const performSearchCall = async () => {
    console.log("Making call");
    setSubmittingSearch(false);

    const { data } = await client.query({
      query: SEARCH_GQL,
      variables: { searchTerm: usableSearchTerm },
    });

    console.log("Data ", data);
    setSearchResults(data.search.results);
    setRedirectToResults(true);

    //TODO: error handling

    // if (termHasMinimumLength(searchTerm)) {
    //   //TODO: sanitize search term before sending to server
    //   searchApi
    //     .searchApiCall(searchTerm, 0, 10)
    //     .then(resultData => {
    //       setSubmittingSearch(false)
    //       // with spread: same state but override typing with false, and searchResults becomes the current
    //       // 'searchResults' from API call (shortcut of {searchResults: searchResults})
    //       console.log("SearchRes ", resultData.results[0]);
    //       setSearchResults(resultData.results);
    //       setIsTyping(true);
    //       setRedirectToResults(true)

    //       //setSearchState({ ...searchState, searchResults: resultData.searchResults, searchTerm, termUsed: searchTerm });

    //       // this.setState({ ...this.state, typing: false, searchResults: resultData.results, searchTerm, termUsed: searchTerm }, () => {
    //       //   //TODO: this is here to avoid re-rendering 
    //       //   this.setState({ ...this.state, typing: true, searchTerm });
    //       // });
    //     });
  }




  const onSubmit = async (e: React.FormEvent) => {
    e.preventDefault();
    window.scrollTo(0, 0);
    setIsTyping(false);


    //setSearchTerm()
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

    console.log("Setting search term:", target.value);
    setSearchTerm(target.value);
  }

  // if (data && data.search) {
  //   console.log("Data arrived hook: ", data.search);
  //   setIsTyping(true);
  //   setSearchResults(data.search.results);
  //   setRedirectToResults(true)
  // }

  return (
    <>
      <SearchBox
        onSubmit={onSubmit}
        onChange={updateSearchTerm}
        searchTerm={searchTerm} />

      {redirectToResults &&
        <RenderRedirect termUsed={searchTerm} searchResults={searchResults} />
      }
      {/* {loading &&
        <div>Loading</div>
      }
      {error &&
        <div>ERROR </div>
      } */}
    </>
  );
}

export default SearchInTopBar;