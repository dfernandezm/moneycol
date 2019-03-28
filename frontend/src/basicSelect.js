import React from 'react';

class BasicSelect extends React.Component {
  constructor(props) {
    super(props)
    this.handleChange = this.handleChange.bind(this);
  }
  handleChange(e) {
    this.props.setValue(e.target.value);
  }
  render(props) {
    const options = this.props.options.map(elem => (
      <option key={1} value={elem}>{elem}</option>
    ));
    return (
      <select value={this.props.value} onChange={this.handleChange}>
      {options}
      </select>
    );
  }
}

export default BasicSelect;

