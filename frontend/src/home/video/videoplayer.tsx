import React, { useState, useEffect } from 'react';
import { Player } from 'video-react';
import 'video-react/dist/video-react.css';

import { VideoP } from './videop';
import TextField from "@material-ui/core/TextField";
import Typography from "@material-ui/core/Typography";
import Container from "@material-ui/core/Container";

// look here -----> https://stackoverflow.com/questions/41303012/updating-source-url-on-html5-video-with-react/41303748
const VideoPlayer: React.FC = () => {
  const url1 = "http://184.72.239.149/vod/smil:BigBuckBunny.smil/playlist.m3u8";
  const url2 = "https://media.w3.org/2010/05/sintel/trailer_hd.mp4";
  const [videoUrl, setVideoUrl] = useState(url2);

  useEffect(() => {
    console.log("Video url>>>>>: ", videoUrl);
  }, [videoUrl]);

  //https://github.com/DefinitelyTyped/DefinitelyTyped/blob/master/types/react/index.d.ts#L485


  const handlePaste = (event: React.ClipboardEvent) => {
    event.clipboardData.items[0].getAsString(text => {
      // do something
      setVideoUrl(text);
      //console.log(">>>>>>Video url: ", text);
    })
    //setVideoUrl(event.clipboardData.getData('Text'));

  };

  const handleChange = (event: React.ChangeEvent<HTMLInputElement>) => {
    setVideoUrl(event.target.value);
  }


  return (
    <>

      <Container component="main" maxWidth="lg">
        <Typography component="h1" variant="h5"> Video player</Typography>
        <TextField
          variant="outlined"
          margin="normal"
          fullWidth
          id="videoUrl"
          onPaste={handlePaste}
          onChange={handleChange}
          value={videoUrl}
          label="Video URL"
          name="videoUrl"
        />
      </Container>
      <Container component="main" maxWidth="md">
        <VideoP videoUrl={videoUrl} />
      </Container>
    </>
  );

}

export { VideoPlayer };