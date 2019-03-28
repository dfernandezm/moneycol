import React, { Component, Fragment } from "react";

import M from "materialize-css";

class AutocompleteMaterial extends Component {

  constructor(props) {
    super(props);
    //TODO: read options from props
  } 

  componentDidMount() {
    
    let elements = document.querySelectorAll('.autocomplete');
 
    let options = {
      data: {
      "Afganistan": null,
      "Albania": null,
      "Barbados": null,
      "Bermuda": null,
      "Jersey": null,
      "Guernsey": null,
      "Spain": null,
      "Laos": null,
      "Cambodia": null,
      "Netherlands": null
    }
  }
    this.autocompletes = M.Autocomplete.init(elements, options);
  }

  render() {

    return (
      <Fragment>

        <div className="row">
          <div className="col s12">
            <div className="row">
              <div className="input-field col s12">
                <i className="material-icons prefix">textsms</i>
                <input type="text" id="autocomplete-input" className="autocomplete"/>
                <label htmlFor="autocomplete-input">Country</label>
              </div>
            </div>
          </div>
        </div>
        
      </Fragment>
    );
  }
}

export default AutocompleteMaterial;
