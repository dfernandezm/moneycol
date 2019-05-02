import React from 'react';
import M from 'materialize-css';

import searchApi from '../apiCalls/searchApi';
import queryString from 'query-string';

import SearchBar from './searchBar';

import {
  Redirect
} from "react-router-dom";

// https://www.robinwieruch.de/react-fetching-data/

// we want a form with a free text search that would link directly to server side
// in the future, a form with 3 Selects: country, year and denomination (more useful for adding than searching)

class SearchInTopBar extends React.Component {

  constructor(props) {
    super(props);
    this.state = {
      typing: true,
      searchTerm: "",
      searchResults: []
    };

    this.onSubmit = this.onSubmit.bind(this);
  }

  // We have to call 'searchFromUrl' here as well in case a direct link to /search?qs=term is invoked first time round
  componentDidMount() {
    M.updateTextFields();
  }

  termHasMinimumLength() {
    return this.state.searchTerm.length > 3
  }

  shouldRenderResults() {
    // not typing and minimum term length for searching
    console.log("Should render the results!!!");
    return !this.state.typing && this.termHasMinimumLength();
  }

  performSearchCall() {
    const searchTerm = this.state.searchTerm
    console.log("Term: " + searchTerm);
    if (this.termHasMinimumLength()) {
      //TODO: sanitize search term before sending to server
      //TODO: Should return this promise if something else should be updated
      searchApi
        .searchApiCall(searchTerm)
        .then(searchResults => {
          // with spread: same state but override typing with false, and searchResults becomes the current
          // 'searchResults' from API call (shortcut of {searchResults: searchResults})
          this.setState({...this.state, typing: false, searchResults }, () => {
            // this.props.history.push({
            //   pathname: '/search',
            //   search: '?qs=' + this.state.searchTerm
            // })
            console.log("data fetched");
          });
        });
    } 
  }

  // ================ With Submit button ============
  onSubmit(e) {
    console.log("On Submit!!");
    e.preventDefault();
    this.performSearchCall(this.state);
  }

  updateSearchTerm = event => {
      this.setState({
        ...this.state,
        typing: true,
        searchResults: [],
        searchTerm: event.target.value
      });
  }

  render() {
      return (
        <div className="search container">
          <SearchBar
                  onSubmit={this.onSubmit} 
                  onChange={this.updateSearchTerm}
                  searchTerm={this.state.searchTerm} />
          { this.shouldRenderResults() &&
            <Redirect to={
              {
                pathname: '/searchResultsPage',
                search: '?qs=' + this.state.searchTerm,
                state: { results: this.state.searchResults }
              }
            } />
          }
        </div>
      );
  }
}

export default SearchInTopBar;