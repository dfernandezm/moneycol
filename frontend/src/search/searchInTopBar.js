import React from 'react';
import M from 'materialize-css';

import searchApi from '../apiCalls/searchApi';
import SearchBar from './searchBar';
import { Redirect } from "react-router-dom";

// This components controls the search form state and rendering of results through redirect to results page
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

  componentDidMount() {
    M.updateTextFields();
  }

  termHasMinimumLength() {
    return this.state.searchTerm.length > 3
  }

  shouldRenderResults() {
    // not typing and minimum term length for searching
    return !this.state.typing && this.termHasMinimumLength();
  }

  performSearchCall() {
    const searchTerm = this.state.searchTerm
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