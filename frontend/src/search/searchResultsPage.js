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
      searchResults: null,
      totalResultLength: 500,
      hasMore: true,
      from: 0
    };
  }

  shouldRenderResults() {
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
        // store values from URL in the state of this component
       const queryStringValues = queryString.parse(this.props.location.search);
       this.setState({...this.state,  searchTerm: queryStringValues.qs, searchResults: this.props.location.state.results})
    }

  }

  searchFromUrlTermIfFound() {
    const queryStringValues = queryString.parse(this.props.location.search);
    if (queryStringValues.qs) {
      // We run the search call in a callback passed to setState to ensure it sees the mutated the state
      console.log("Search term: " + queryStringValues.qs);
      this.setState({...this.state, searchTerm: queryStringValues.qs}, this.performSearchCall);
    }
  }

  termHasMinimumLength() {
    return this.state.searchTerm.length > 3;
  }

  performSearchCall() {
    const searchTerm = this.state.searchTerm
    console.log("Another call with " + searchTerm);
    if (this.termHasMinimumLength()) {
      console.log("Making call");
      searchApi
        .searchApiCall(searchTerm, this.state.from, 10)
        .then(searchResults => {
            this.updateStateWith(searchResults);
        });
    } 
  }

  updateStateWith(newSearchResults) {

      this.setState({...this.state,
        from: this.state.from + 10,
        searchResults: this.state.searchResults.concat(newSearchResults)
      });
  }

  fetchMoreData = () => {
    console.log("More data");
    if (this.state.searchResults.length >= this.state.totalResultLength) {
      this.setState({ hasMore: false });
      return;
    }

    // a fake async api call like which sends
    // 20 more records in 1.5 secs

    // setTimeout(() => {
    //   this.setState({
    //     searchResults: this.state.searchResults.concat(Array.from({ length: 20 }))
    //   });
    // }, 1500);

    this.performSearchCall();



  };

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
              dataLength={this.state.searchResults.length}
              next={this.fetchMoreData}
              hasMore={this.state.hasMore}
              height={200}
              loader={<h4>Loading...</h4>}>
              {this.state.searchResults.map((result, index) => (
                <div style={style} key={index}>
                  div - #{index} - {result.BanknoteName}
                </div>
              ))}
          </InfiniteScroll>
          }
        </div>
      );
  }
}

export default SearchResultsPage;