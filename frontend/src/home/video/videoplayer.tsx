import React, { useState, useEffect } from 'react';
import { Player } from 'video-react';
import 'video-react/dist/video-react.css';

import Button from "@material-ui/core/Button";
import TextField from "@material-ui/core/TextField";
import Typography from "@material-ui/core/Typography";
import Container from "@material-ui/core/Container";

const VideoPlayer: React.FC = () => {
  const url1 = "https://cdndaily11.azureedge.net/daily/1/playlist.m3u8";
  const url2 = "https://media.w3.org/2010/05/sintel/trailer_hd.mp4";
  const [videoUrl, setVideoUrl] = useState("");

  useEffect(()=> {
    console.log("Video url>>>>>: ", videoUrl);
  },[videoUrl]);

  //https://github.com/DefinitelyTyped/DefinitelyTyped/blob/master/types/react/index.d.ts#L485


  const handlePaste = (event: React.ClipboardEvent) => {
    event.clipboardData.items[0].getAsString(text=>{
      // do something
      setVideoUrl(text);
     //console.log(">>>>>>Video url: ", text);
    })
    //setVideoUrl(event.clipboardData.getData('Text'));
    
  };

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
          value={videoUrl}
          label="Video URL"
          name="videoUrl"
        />
      </Container>
      <Container component="main" maxWidth="md">
        <Player>
          <source src={videoUrl} />
        </Player>
      </Container>
    </>
  );

}

export { VideoPlayer };