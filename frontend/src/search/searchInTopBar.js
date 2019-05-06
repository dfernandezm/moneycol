import React from 'react';
import M from 'materialize-css';

import searchApi from '../apiCalls/searchApi';
import SearchBar from './searchBar';
import RenderRedirect from './renderRedirect';

// This components controls the search form state and rendering of results through redirect to results page
class SearchInTopBar extends React.Component {

  constructor(props) {
    super(props);
    this.state = {
      typing: true,
      searchTerm: "",
      termUsed: "",
      searchResults: []
    };

    this.onSubmit = this.onSubmit.bind(this);
    this.termHasMinimumLength = this.termHasMinimumLength.bind(this);
  }

  componentDidMount() {
    M.updateTextFields();
  }

  termHasMinimumLength() {
    return this.state.searchTerm.length > 3;
  }

  shouldRenderResults() {
    let isGoingToRender = !this.state.typing && this.termHasMinimumLength();
    return isGoingToRender;
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
          this.setState({...this.state, typing: false, searchResults, searchTerm, termUsed: searchTerm }, () => {
            // this.props.history.push({
            //   pathname: '/search',
            //   search: '?qs=' + this.state.searchTerm
            // })
            //TODO: this is here to avoid re-rendering 
            this.setState({typing: true, searchTerm: ""});
          });
        });
    } 
  }

  //TODO: get here the 'value' of the form input (searchTerm)
  onSubmit(e) {
    e.preventDefault();
    this.performSearchCall(this.state);
  }

  //TODO: not needed until submit
  updateSearchTerm = event => {
      this.setState({
        ...this.state,
        typing: true,
        searchResults: [],
        searchTerm: event.target.value
      });
  }

  render() {
      let termUsed = this.state.termUsed;
      let searchResults = this.state.searchResults;
      console.log("Rendering");
      return (
        <div className="search container">
          <SearchBar
                  onSubmit={this.onSubmit} 
                  onChange={this.updateSearchTerm}
                  searchTerm={this.state.searchTerm} />
          { this.shouldRenderResults() &&
           <RenderRedirect termUsed={termUsed} searchResults={searchResults} />
          }
        </div>
      );
  }
}

export default SearchInTopBar;