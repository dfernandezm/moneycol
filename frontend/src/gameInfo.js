import React from 'react';

const GameInfo = (props) => {
  return ( 
  <div className="game-info">
    <div>{props.status}</div>
    <ul>{props.moves}</ul>
  </div> 
  )
}

export default GameInfo;