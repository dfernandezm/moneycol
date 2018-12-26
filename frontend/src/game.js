import React from 'react';
import Board from './board';

export default class Game extends React.Component {
    constructor(props) {
      super(props);
      this.state = {
        history: [{
          squares: Array(9).fill(null)
        }],
        stepNumber: 0,
        next: 'X'
      }
      this.handleClick = this.handleClick.bind(this);
    }

    handleClick(i) {
       return () => {

            //let history = this.state.history;
            // Discard history if move done in midle
            const history = this.state.history.slice(0, this.state.stepNumber + 1);
            let current = history[history.length - 1];
            let squares = current.squares.slice() 
            
            this.exitIfFilledOrWinner(squares, i);
        
            squares[i] = this.state.next;
    
            this.setState({
                // concat does not mutate, preferred over .push()
                history: history.concat([{
                    squares: squares
                }]),
                stepNumber: history.length,
                next: this.decideNext()
            })
        }    
    }
  
    decideNext() {
      if (this.state.next === 'X') {
          return 'O';
      } 
      return 'X';
    }

    exitIfFilledOrWinner(squares, i) {
        if (squares[i] || calculateWinner(squares)) {
            return;
        }
    }

    jumpTo(stepNumber) {
        this.setState({
          stepNumber: stepNumber,
          next: (stepNumber % 2) === 0 ? 'X' : '0',
        });
      }
  
    render() {
       
        let history = this.state.history;
        let current = history[this.state.stepNumber];
        let winner = calculateWinner(current.squares)
        const status = (winner) ? `Winner: ${winner}` : 
                                ((areAllFilled(current.squares)) ? 
                                    `There is a tie!, Start over!`:    
                                    `Next player: ${this.state.next}`);

        let moves = history.map((step,moveIndex) => {
            const description = moveIndex ? `Jump to move #${moveIndex}` : "Go to game start"
            return (
                <li key={moveIndex}> 
                    <button onClick={() => this.jumpTo(moveIndex)}>{description}</button>
                </li>
            )
        })                            

      return (
        <div className="game">
          <div className="game-board">
            <Board squares={current.squares} onClick={this.handleClick} />
          </div>
          <div className="game-info">
            <div>{status}</div>
            <ol>{moves}</ol>
          </div>
        </div>
      );
    }
  }
  
  function calculateWinner(squares) {
    const lines = [
      [0, 1, 2],
      [3, 4, 5],
      [6, 7, 8],
      [0, 3, 6],
      [1, 4, 7],
      [2, 5, 8],
      [0, 4, 8],
      [2, 4, 6],
    ];
    for (let i = 0; i < lines.length; i++) {
      const [a, b, c] = lines[i];
      if (squares[a] && squares[a] === squares[b] && squares[a] === squares[c]) {
        return squares[a];
      }
    }
    return null;
}

function areAllFilled(squares) {
    return squares.reduce((prev,curr,index,arr) => {
        return prev && curr != null;
    }, true)
}