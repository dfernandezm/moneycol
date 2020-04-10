import React, { useEffect, useState } from 'react';

import RenderRedirect from './redirectToResultsPage';
import SearchBox from '../navbar/searchBox';

const SearchInTopBar: React.FC = () => {

  const [submittingSearch, setSubmittingSearch] = useState(false);
  const [searchTerm, setSearchTerm] = useState('');
  const [usableSearchTerm, setUsableSearchTerm] = useState('');
  const [redirectToResults, setRedirectToResults] = useState(false);

  // react docs: pass empty array here to indicate that this does not depend on state or props (run only once)

  // When submit button has been clicked, 
  useEffect(() => {
    if (submittingSearch) {
      setRedirectToResults(true);
      setUsableSearchTerm(searchTerm);
    }
  }, [searchTerm, submittingSearch])

  // While typing, we only consider search term with at least 3 characters
  useEffect(() => {
    if (searchTerm.length > 2) {
      setUsableSearchTerm(searchTerm);
    }
  }, [searchTerm])

  const onSubmit = async (e: React.FormEvent) => {
    e.preventDefault();
    window.scrollTo(0, 0);
    setSubmittingSearch(true);
  }

  // //TODO: this could be optimized by only re-rendering the single input as it's typed on --
  // // right now it re-renders the whole search component (fully controlled)
  const updateSearchTerm = (e: React.FormEvent) => {
    const target = e.target as HTMLInputElement;
    setSubmittingSearch(false);
    setRedirectToResults(false);
    setSearchTerm(target.value);
  }

  return (
    <>
      <SearchBox
        onSubmit={onSubmit}
        onChange={updateSearchTerm}
        searchTerm={searchTerm} />

      {redirectToResults &&
        <RenderRedirect termUsed={usableSearchTerm} />
      }
    </>
  );
}

export default SearchInTopBar;