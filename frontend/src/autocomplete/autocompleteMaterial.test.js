import React from 'react';
import ReactDOM from 'react-dom';
import AutocompleteMaterial from './autocompleteMaterial';
import renderer from 'react-test-renderer';

it('renders Autocomplete without crashing', () => {
  const div = document.createElement('div');
  ReactDOM.render(<AutocompleteMaterial />, div);
  ReactDOM.unmountComponentAtNode(div);
});


it('renders Autocomplete with Country label', () => {
 
  const component = renderer.create(
    <AutocompleteMaterial/>,
  );

  let tree = component.toJSON();
  //To update the snapshot, run type u if you are still in watch mode, 
  //or simply delete the snapshot file under the __snapshots__ folder
  expect(tree).toMatchSnapshot();  

});

it('it initiates only 1 autocomplete materialize css', () => {
 
  const component = renderer.create(
    <AutocompleteMaterial/>
  );
  //TODO: internal behaviour, consider design of this
  expect(component.getInstance().autocompletes).toBeDefined();
});

xit('it renders value "Afganistan" when typing "afg"', () => {
 
  // const component = renderer.create(
  //   <AutocompleteMaterial/>
  // );
  
});