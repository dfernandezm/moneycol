import React from 'react';
//import BasicSelect from './basicSelect';

class YearSelect extends React.Component {
  constructor(props) {
      super(props);
      this.state = { selected: null, value: '' };
      this.handleChange = this.handleChange.bind(this)
  }

  handleChange(event) {
    console.log(event);
  }

  setValue(val) {
    console.log("SetValue");
  }

  render() {
      // const options = ["1","2"]
      // const drop =  <Dropdown options={options} onChange={this.handleChange} value={this.state.value} placeholder="Select an option" />
      // const trickySelect =  <Select
          //   value={this.state.value}
          //   onChange={this.handleChange}
          //   inputProps={{
          //     name: 'age',
          //     id: 'age-simple',
          //   }}>
          //   <MenuItem value="">
          //     <em>None</em>
          //   </MenuItem>
          //   <MenuItem value={10}>Ten</MenuItem>
          //   <MenuItem value={20}>Twenty</MenuItem>
          //   <MenuItem value={30}>Thirty</MenuItem>
          // </Select>
          const theOptions = ["1","2","3"]
      return (
      <div className="input-field col s4 left">
         {/* <BasicSelect options={theOptions} setValue={this.setValue}/> */}
      </div>
      )
  }
}

export default YearSelect;
