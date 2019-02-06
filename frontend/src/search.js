import React from 'react';
import Autocomplete from './autocompleteManual';
import YearSelect from './yearSelect';
import BasicSelect from './basicSelect';

class Search extends React.Component {
  setValue(val) {
    console.log("SetValue");
  }
  render() {
    const theOptions = ["1","2","3"]
    return (
      <div className="search container">
        <div className="row">
          <div className="input-field col s4 left">
            <Autocomplete suggestions={[
              "Afganistan",
              "Albania",
              "Barbados",
              "Bermuda",
              "Jersey",
              "Guernsey",
              "Spain",
              "Laos",
              "Cambodia",
              "Netherlands"
            ]}/>
          </div>
          <YearSelect/>
      </div>
      <div className="row">
        <div className="input-field col s4 left">
            <input id="value" type="text" />
            <label htmlFor="value">Value</label>
        </div> 
        <div className="input-field col s4 left">
            <input id="name" type="text" />
            <label htmlFor="name">Name</label>
        </div> 
      </div>
      <div> <h3>Hey</h3>
      <BasicSelect options={theOptions} setValue={this.setValue}/>
      </div>
      </div>
    );
  }
}

export default Search