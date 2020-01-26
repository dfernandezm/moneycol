import React, { useState, useEffect } from 'react';

import { Player } from 'video-react';
import 'video-react/dist/video-react.css';

const VideoP: React.FC<any> = ({ videoUrl }) => {
    const [uri, setUri] = useState(videoUrl);
    const player = (
        <Player key={uri}>
            <source src={uri} />
        </Player>
    )
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


