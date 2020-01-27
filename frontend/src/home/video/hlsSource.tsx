import React, { useState, useEffect } from 'react';
import Hls from 'hls.js';

const HLSSource: React.FC<any> = ({ src, video, type }) => {
    const [hls, _] = useState(new Hls());

    useEffect(() => {
        if (Hls.isSupported()) {
            hls.loadSource(src);
            hls.attachMedia(video);
            hls.on(Hls.Events.MANIFEST_PARSED, () => {
              video.play();
            });

            return () => {
                hls.destroy();
            }
        }
    },[])

    return (


            <source
              src={src}
              type={type || 'application/x-mpegURL'}
            />
      

    );
}

export { HLSSource };


