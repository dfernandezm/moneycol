import React from 'react';
import M from 'materialize-css';

import SearchResultsList from './searchResultsList';
import EmptyResults from './emptySearchResults';
import searchApi from '../apiCalls/searchApi';
import InfiniteScroll from 'react-infinite-scroll-component';

const queryString = require('query-string');

//TODO: If we want bookmarks, this component should probably re-search if state is not present and the url contains the search term
class SearchResultsPage extends React.Component {

  constructor(props) {
    super(props);
    this.state = {
      typing: true,
      searchTerm: "",
      termUsed: "",
      searchResults: null
    };
  }

  shouldRenderResults() {
    // not typing and minimum term length for searching
    //console.log("State from redirection: " , this.props.location);
    let hasBeenRedirected = this.props.location !== undefined;
    let hasResultsToShow = this.props.location.state && 
                            this.props.location.state.results && 
                            this.props.location.state.results.length > 0;

    return (hasBeenRedirected && hasResultsToShow) || this.state.searchResults.length > 0;
  }

  componentDidUpdate(prevProps) {
    // When url param 'qs' changes, we want to re-render the component as it has to search with new qs
    if (this.props.location.search !== prevProps.location.search) { 
      this.searchFromUrlTermIfFound();
    }
  }

  // We have to call 'searchFromUrl' here as well in case a direct link to /search?qs=term is invoked first time round
  componentDidMount() {
    M.updateTextFields();
    let hasResultsToShow = this.props.location.state && 
                            this.props.location.state.results && 
                            this.props.location.state.results.length > 0;

    if(!hasResultsToShow) {
      this.searchFromUrlTermIfFound();
    } else {
       this.setState({...this.state,  searchResults: this.props.location.state.results})
    }

  }

  searchFromUrlTermIfFound() {
    const queryStringValues = queryString.parse(this.props.location.search);
    if (queryStringValues.qs) {
      // We run the search call in a callback passed to setState to ensure it sees the mutated the state
      this.setState({...this.state, searchTerm: queryStringValues.qs}, this.performSearchCall);
    }
  }

  termHasMinimumLength() {
    return this.state.searchTerm.length > 3;
  }

  performSearchCall() {
    const searchTerm = this.state.searchTerm
    if (this.termHasMinimumLength()) {
      //TODO: sanitize search term before sending to server
      searchApi
        .searchApiCall(searchTerm)
        .then(searchResults => {
            this.updateStateWith(searchResults);
        });
    } 
  }

  updateStateWith(newSearchResults, searchTerm) {
      // with spread: same state but override typing with false, and searchResults becomes the current
      // 'searchResults' from API call (shortcut of {searchResults: searchResults})
      // this.setState({...this.state, typing: false, searchResults, searchTerm, termUsed: searchTerm }, () => {
      //   this.setState({typing: true, searchTerm });
      // });

      // this.setState({
      //   items: this.state.searchResults.concat(newSearchResults))
      // });
  }

  render() {
    const style = {
      height: 30,
      border: "1px solid green",
      margin: 6,
      padding: 8
    };
      return (
        <div className="searchResults">
          { this.state.searchResults === null ? null : 
            <InfiniteScroll
            dataLength={this.state.totalResultLength}
            next={this.fetchMoreData}
            hasMore={true}
            loader={<h4>Loading...</h4>}>
            {this.state.searchResults.map((result, index) => (
              <div style={style} key={index}>
                div - #{index}
              </div>
            ))}
          </InfiniteScroll>
          }
        </div>
      );
  }
}

export default SearchResultsPage;