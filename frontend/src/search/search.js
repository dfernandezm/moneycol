import React from 'react';
import M from 'materialize-css';

//TODO: for now we are leaving here the autocomplete as it initialises materialize. If not, the click in
// the input does not move the label up and also typing does not replace the placeholder in this Search Component
//import AutocompleteMaterial from '../autocompleteMaterial';

import searchApi from '../apiCalls/searchApi';
import SearchForm from './searchForm';
import SearchResultList from './searchResults';
import EmptyResults from './emptySearchResults';

// https://www.robinwieruch.de/react-fetching-data/

// we want a form with a free text search that would link directly to server side
// in the future, a form with 3 Selects: country, year and denomination (more useful for adding than searching)

class Search extends React.Component {

  constructor() {
    super();
    this.state = {
      typing: true,
      typingTimeout: () => {},
      searchTerm: "",
      searchResults: []
    };
  }

  componentDidMount() {
    M.updateTextFields();
  }

  termHasMinimumLength() {
    return this.state.searchTerm.length > 3
  }

  shouldRenderResults() {
    // not typing and minimum term length for searching
    return !this.state.typing && this.termHasMinimumLength()
  }

  performSearchCall(state) {
    console.log("Term: " +  state.searchTerm);
    if (this.termHasMinimumLength()) {
      //TODO: sanitize search term before sending to server
      searchApi
      .searchApiCall(state.searchTerm)
      .then(searchResults => {
          state.typing = false;
          state.searchResults = searchResults;
          this.setState(state);
          //console.log(self.state);
      })
    }
  }

  //TODO: wait a delay:
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
  render() {
      return (
        <div className="search container">
         <SearchForm updateInputOnChange={this.updateInput} searchTerm={this.state.searchTerm} />
        { this.shouldRenderResults() ? 
          <SearchResultList resultList={this.state.searchResults} />
          : <EmptyResults message="No results yet!" />
        }
        </div>
      );
  }
}

export default Search