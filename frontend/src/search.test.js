import React from 'react';
import ReactDOM from 'react-dom';
import Search from './search';
import renderer from 'react-test-renderer';
import { shallow } from 'enzyme';

it('renders Autocomplete without crashing', () => {
  const div = document.createElement('div');
  ReactDOM.render(<Search />, div);
  ReactDOM.unmountComponentAtNode(div);
});
// for snapshots
// https://blog.usejournal.com/testing-with-jest-and-enzyme-in-react-part-6-snapshot-testing-in-jest-72fb0ce91c5a
// react test form submission 
it('wont render results with term less than 3 letters', () => {
  const wrapper = shallow(<Search />);
  const event = {target: {name: "searchTerm", value: "Un"}};
  const searchInput = wrapper.find('input').at(0);
  
  searchInput.simulate('change', event);
  expect(wrapper.find('span').text()).toBe('No results yet!');
  
});


