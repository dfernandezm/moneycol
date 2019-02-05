import React from 'react';
import SearchSuggest from './searchSuggest';
import SearchAuto from './searchAuto';
import Autocomplete from './autocompleteManual';

class Search extends React.Component {
  
  render() {

    return (
      <div className="search container">
         <h3>Search</h3>
         {/* <div class="row">
    <div class="col s12">
      <div class="row">
        <div class="input-field col s12">
          <i class="material-icons prefix">textsms</i>
          <input type="text" id="autocomplete-input" class="autocomplete"/>
          <label for="autocomplete-input">Type a country</label>
        </div>
      </div>
    </div>
  </div> */}
        <Autocomplete suggestions={[
          "Alligator",
          "Bask",
          "Crocodilian",
          "Death Roll",
          "Eggs",
          "Jaws",
          "Reptile",
          "Solitary",
          "Tail",
          "Wetlands"
        ]}/>
      </div>
    );
  }
}

export default Search