import React, { useState, useEffect } from 'react';

import { Player } from 'video-react';
import 'video-react/dist/video-react.css';

const VideoP: React.FC<any> = ({videoUrl}) => {
return (
 
 
        <Player>
          <source src={videoUrl} />
        </Player>
        
        );
}

export { VideoP };


