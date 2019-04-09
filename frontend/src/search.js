import React from 'react';
//import Autocomplete from './autocompleteManual';
import YearSelect from './yearSelect';
//import BasicSelect from './basicSelect';
import AutocompleteMaterial from './autocompleteMaterial';

// we want a form with a free text search that would link directly to server side
// alternatively, a form with 3 Selects: country, year and denomination (more useful for adding new banknote)

// https://www.robinwieruch.de/react-fetching-data/

const API = 'http://localhost:4000/search?qs=';
const DEFAULT_QUERY = 'Unidos';

class Search extends React.Component {

  constructor() {
    super();
    this.state = {
      typing: true,
      typingTimeout: ()=>{},
      searchTerm: "",
      searchResults: []
    };
  }

  componentDidMount() {
    // fetch(API + (this.state.searchTerm || DEFAULT_QUERY))
    //   .then(response => response.json())
    //   .then(data => this.setState({...this.state, searchResults: data.searchResults }));
  }

  termHasMinimumLength() {
    return this.state.searchTerm.length > 3
  }

  shouldRenderResults() {
    // not typing and minimum term length for searching
    return !this.state.typing && this.termHasMinimumLength()
  }

  //TODO:  wait a delay:
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
      typingTimeout: setTimeout(function () {
          console.log("Term: " +  self.state.searchTerm);
          if (self.termHasMinimumLength()) {
            //TODO: sanitize search term before sending to server
            fetch(API + self.state.searchTerm)
              .then(response => response.json())
              .then(data => {
                console.log("Data is: ", data.searchResults);
                console.log("Results is: ", data.searchResults.results);
                self.state.typing = false;
                self.state.searchResults = data.searchResults.results;
                self.setState(self.state);
                console.log(self.state);
            });
          }
        }, 1000)
   });

  }

  render() {

    //console.log(this.state)
      return (
        <div className="search container">
          <div className="row">
            <div className="input-field col s4 left">
                <input id="searchTerm" type="text" 
                name="searchTerm"
                onChange={this.updateInput}
                value={this.state.searchTerm} />
                <label htmlFor="searchTerm">Search Query</label>
            </div> 
          </div>
        { this.shouldRenderResults() ? 
          (
            <div className="row">
              <div className="results">    
                Results: <br />
                <ul>
                  {this.state.searchResults.map(banknote =>
                    <li key={banknote.CatalogCode}>
                      {banknote.Country}: {banknote.BanknoteName} ({banknote.Year})
                    </li>
                  )}
                </ul>
              </div>      
            </div> 
          ) : (
            <span>No results yet!</span> 
          )
        }
        </div>
      );
  }
}

export default Search