import React from 'react';
import ReactDOM from 'react-dom';
import Search from './search';
import renderer from 'react-test-renderer';
import { shallow, mount } from 'enzyme';

it('renders Autocomplete without crashing', () => {
  const div = document.createElement('div');
  ReactDOM.render(<Search />, div);
  ReactDOM.unmountComponentAtNode(div);
});

// for snapshots
// https://blog.usejournal.com/testing-with-jest-and-enzyme-in-react-part-6-snapshot-testing-in-jest-72fb0ce91c5a
// react test form submission 

// mount vs shallow
// shallow does not render nested components, mount does

// expect (jest)
// https://jestjs.io/docs/en/expect

it('wont render results with term less than 3 letters', () => {
  const wrapper = mount(<Search />);
  const event = {target: {name: "searchTerm", value: "Un"}};
  const searchInput = wrapper.find('input').at(0);
  
  searchInput.simulate('change', event);
  expect(wrapper.find('h5').text()).toBe('No results yet!');
  
});

//https://www.npmjs.com/package/jest-fetch-mock
xit('does not render empty when term is greater than 3 chars', (done) => {
  const wrapper = shallow(<Search />);
  const event = {target: {name: "searchTerm", value: "Andorra"}};
  const searchInput = wrapper.find('input').at(0);
  //console.log(">>>>>>>> Yayyyyyyy");
  searchInput.simulate('change', event);
  setTimeout(() => {
    console.log(">>>>>>>>" + wrapper.find('span').text())
  expect(wrapper.find('span').text()).not.toBe('No results yet!');
  done()
  }, 800);

});

