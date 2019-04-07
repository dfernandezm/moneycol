import React from 'react';
import M from 'materialize-css';

//TODO: for now we are leaving here the autocomplete as it initialises materialize. If not, the click in
// the input does not move the label up and also typing does not replace the placeholder in this Search Component
//import AutocompleteMaterial from '../autocompleteMaterial';

import searchApi from '../apiCalls/searchApi';
import queryString from 'query-string';

import SearchFormInline from './searchFormInline';
import SearchResultList from './searchResults';
import SearchResultCard from './searchResultCard';
import EmptyResults from './emptySearchResults';


// https://www.robinwieruch.de/react-fetching-data/

// we want a form with a free text search that would link directly to server side
// in the future, a form with 3 Selects: country, year and denomination (more useful for adding than searching)

class Search extends React.Component {

  constructor(props) {
    super(props);
    this.state = {
      typing: true,
      typingTimeout: () => {},
      searchTerm: "",
      searchResults: []
    };

    this.onSubmit = this.onSubmit.bind(this);
  }

  // Component doesn't re-render when the props change (location is a prop that needs to be passed in from Router)
  // In order to access it we have them in the constructor (props)
  // So when a component is already mounted and the qs changes rerender won't trigger. 
  // You will have to trigger the update by updating the state. 
  // We use componentDidUpdate instead of componentWillReceiveProps since it is marked as UNSAFE
  // https://stackoverflow.com/questions/52539039/reactjs-re-render-same-component-when-navigate-back
  componentDidUpdate(prevProps, prevState) {
    // When url param 'qs' changes, we want to re-render the component as it has to search with new qs
    if (this.props.location.search !== prevProps.location.search) { 
      this.searchFromUrlTermIfFound();
    }
  }

  // We have to call 'searchFromUrl' here as well in case a direct link to /search?qs=term is invoked first time round
  componentDidMount() {
    M.updateTextFields();
    this.searchFromUrlTermIfFound();
  }

  searchFromUrlTermIfFound() {
    console.log(this.props.location.search);
    const queryStringValues = queryString.parse(this.props.location.search);
    console.log(queryStringValues.qs);
    //TODO: sanitize qs before sending to server
    if (queryStringValues.qs) {
      // We run the search call in a callback passed to setState to ensure it sees the mutated the state
      this.setState({...this.state, searchTerm: queryStringValues.qs}, this.performSearchCall);
    }
  }

  termHasMinimumLength() {
    return this.state.searchTerm.length > 3
  }

  shouldRenderResults() {
    // not typing and minimum term length for searching
    return !this.state.typing && this.termHasMinimumLength()
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
            this.props.history.push({
              pathname: '/search',
              search: '?qs=' + this.state.searchTerm
            })
          });
        });
    } 
  }

  // ======================= With Search as you type with delay/timeout ====================
  // https://stackoverflow.com/questions/42217121/searching-in-react-when-user-stops-typing
  updateInput = e => {
    let self = this;

    // clear current timeout
    if (self.state.typingTimeout) {
      clearTimeout(self.state.typingTimeout);
    }

    self.setState({
      ...self.state,
      searchTerm: e.target.value,
      typing: true,
      typingTimeout: setTimeout(() => 
          self.performSearchCall(self.state), 1000)
      });
  }

  // ================ With Submit button ============
  onSubmit(e) {
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
          <SearchFormInline
                  onSubmit={this.onSubmit} 
                  onChange={this.updateSearchTerm}
                  searchTerm={this.state.searchTerm} />
          { this.shouldRenderResults() ? 
            <SearchResultCard resultList={this.state.searchResults} /> : 
            <EmptyResults message="No results found" />
          }
        </div>
      );
  }
}

export default Search