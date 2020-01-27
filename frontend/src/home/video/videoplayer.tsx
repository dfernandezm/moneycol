import React, { useState, useEffect } from 'react';
import { Player, ControlBar } from 'video-react';
import 'video-react/dist/video-react.css';

import TextField from "@material-ui/core/TextField";
import Typography from "@material-ui/core/Typography";
import Container from "@material-ui/core/Container";
import { HLSSource } from "./hlsSource";

// Documentation here: https://video-react.js.org/components/player/
// https://video-react.js.org/customize/customize-source/
const VideoPlayer: React.FC = (props) => {
  const url1 = "https://cdndaily11.azureedge.net/daily/18/playlist.m3u8";
  const url3 = "https://www.radiantmediaplayer.com/media/bbb-360p.mp4";
  const url2 = "https://media.w3.org/2010/05/sintel/trailer_hd.mp4";

  const [videoUrl, setVideoUrl] = useState(url1);

  const [thePlayer, setThePlayer] = useState({ load: () => { }, play: () => { } });

  const hlsSource = (
    <HLSSource
      isVideoChild
      src={videoUrl}
    />
  );

  const regularSource = (
    <source src={videoUrl} />
  );

  const [aSource, setASource] = useState(regularSource);


  useEffect(() => {
    console.log("Video url>>>>>: ", videoUrl);

    if (videoUrl.indexOf("m3u8") !== -1) {
      console.log("HLS stream: ", videoUrl);
      setASource(hlsSource);
    } else {
      console.log("Regular stream: ", videoUrl);
      setASource(regularSource);
    }
  }, [videoUrl]);

  //https://github.com/DefinitelyTyped/DefinitelyTyped/blob/master/types/react/index.d.ts#L485

  const reload = () => {

    thePlayer.load();

  }

  const handlePaste = (event: React.ClipboardEvent) => {
    event.clipboardData.items[0].getAsString(text => {

      setVideoUrl(text);
      reload();
    })
  };

  const setPlayerRef = (player: any) => {
    setThePlayer(player);
  }

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
        <Player
          ref={setPlayerRef}
          autoPlay>
          {aSource}
          <ControlBar autoHide={false} />
        </Player>
      </Container>
    </>
  );

}

export { VideoPlayer };