import React, { useState, useEffect } from 'react';

import { Player } from 'video-react';
import 'video-react/dist/video-react.css';

const sources = {
    sintelTrailer: 'http://media.w3.org/2010/05/sintel/trailer.mp4',
    bunnyTrailer: 'http://media.w3.org/2010/05/bunny/trailer.mp4',
    bunnyMovie: 'http://media.w3.org/2010/05/bunny/movie.mp4',
    test: 'http://media.w3.org/2010/05/video/movie_300.webm'
  };

const VideoP: React.FC<any> = ({ videoUrl }) => {
    const [uri, setUri] = useState(videoUrl);
    let currentPlayer:any = null

    useEffect(() => {
        currentPlayer.load()
    },[uri])

    const player = (
        <Player 
            ref={(player: any) => { currentPlayer = player }}>
                <source src={uri} />
        </Player>
    
    );
    
    const player2 = ( 
        <div style={{ width: '800px', height: '600px' }}>
             <video src={videoUrl} controls autoPlay />
        </div>
       
    );

    console.log("prop: " + uri);

    useEffect(() => {
        console.log("Uri changed")
    }, [uri])

    return (

        <>  
        {player}
        </>

    );
}

export { VideoP };


