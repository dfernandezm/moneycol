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

    // Component doesn't re-render when the props change (location is a prop that needs to be passed in from Router)
  // In order to access it we have them in the constructor (props)
  // So when a component is already mounted and the qs changes rerender won't trigger. 
  // You will have to trigger the update by updating the state. 
  // We use componentDidUpdate instead of componentWillReceiveProps since it is marked as UNSAFE
  // https://stackoverflow.com/questions/52539039/reactjs-re-render-same-component-when-navigate-back
  // componentDidUpdate(prevProps, prevState) {
  //   // When url param 'qs' changes, we want to re-render the component as it has to search with new qs
  //   if (this.props.location.search !== prevProps.location.search) { 
  //     //this.searchFromUrlTermIfFound();
      
  //   }
  // }

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