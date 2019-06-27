import React from 'react';
import M from 'materialize-css';

import searchApi from '../apiCalls/searchApi';
import SearchBox from '../navbar/searchBox';
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
            this.setState({typing: true, searchTerm });
          });
        });
    } 
  }

  
  onSubmit(e) {
    e.preventDefault();
    window.scrollTo(0, 0);
    this.performSearchCall(this.state);
  }

  //TODO: this could be optimized by only re-rendering the single input as it's typed on --
  // right now it re-renders the whole search component (fully controlled)
  updateSearchTerm = event => {
      this.setState({
        ...this.state,
        typing: true,
        searchResults: [],
        searchTerm: event.target.value
      });
  }

  render() {

      const { termUsed, searchTerm, searchResults } = this.state;
      
      return (
        <>
          <SearchBox
                  onSubmit={this.onSubmit} 
                  onChange={this.updateSearchTerm}
                  searchTerm={searchTerm} />
          { this.shouldRenderResults() &&
           <RenderRedirect termUsed={termUsed} searchResults={searchResults} />
          }
        </>
      );
  }
}

export default SearchInTopBar;