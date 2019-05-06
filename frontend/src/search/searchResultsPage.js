import React from 'react';
import M from 'materialize-css';

import SearchResultCard from './searchResultCard';
import EmptyResults from './emptySearchResults';

//TODO: If we want bookmarks, this component should probably re-search if state is not present and the url contains the search term
class SearchResultsPage extends React.Component {

  // We have to call 'searchFromUrl' here as well in case a direct link to /search?qs=term is invoked first time round
  componentDidMount() {
    M.updateTextFields();
  }

  shouldRenderResults() {
    // not typing and minimum term length for searching
    console.log("State from redirection: " , this.props.location);
    let hasBeenRedirected = this.props.location !== undefined;
    let hasResultsToShow = this.props.location.state && 
                            this.props.location.state.results && 
                            this.props.location.state.results.length > 0;

    return hasBeenRedirected && hasResultsToShow;
  }

  render() {
      return (
        <div className="searchResults">
          { this.shouldRenderResults() ? 
            <SearchResultCard resultList={this.props.location.state.results} /> : 
            <EmptyResults message="No results found" />
          }
        </div>
      );
  }
}

export default SearchResultsPage;