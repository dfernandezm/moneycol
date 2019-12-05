import React from 'react';
import Collapse from './collapse';
import Label from './label';
// import Content from './content';
const Home = () => {

  return (
    <div className="home">
      <h3> Welcome </h3>
      <Collapse 
        index={0}
        clickablePart={<Label text="Initial title 1"/>}>
          <p>
            The content I want
          </p>
      </Collapse>
      <Collapse 
        index={1}
        clickablePart={<Label text="Initial title 2"/>}>
          <p>
            The content I want
          </p>
      </Collapse>
      <Collapse
        index={2} 
        clickablePart={<Label text="Initial title 3"/>}>
          <p>
            The content I want
          </p>
      </Collapse>
    </div>
  );

}

export default Home;