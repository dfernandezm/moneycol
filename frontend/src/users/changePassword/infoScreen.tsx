import React, { useEffect, useState } from "react";
import Container from "@material-ui/core/Container";
import Typography from "@material-ui/core/Typography";
import { styled } from '@material-ui/core/styles';
import Paper from "@material-ui/core/Paper";
import { Button } from "@material-ui/core";
import { RouteComponentProps } from "react-router-dom";

const StyledPaper = styled(Paper)({
  marginTop: 100,
  display: "flex",
  padding: 20,
  flexDirection: "column",
  alignItems: "center"
});

//TODO: pass in handler for continueUrl more specific:
// handleContinueAfter: (event: React.MouseEvent<HTMLElement>) => void
interface InfoProps {
  message: string,
  buttonText: string,
  buttonDestination: string
  isError?: boolean
}

//TODO: make it more generic so it can receive values from state redirect, or props
type AllInfoProps = InfoProps & RouteComponentProps;

const InfoScreen: React.FC<AllInfoProps> = (props: AllInfoProps) => {

  const [info, setInfo] = useState({
    message: '',
    buttonDestination: '',
    buttonText: '',
    isError: false
  })

  useEffect(() => {   
     
      if (!props.location || !props.location.state) {
        setInfo({ isError: true, 
          message: "Oops, something went wrong", 
          buttonDestination:"/", 
          buttonText: "Return to main page"});
      } else {
        setInfo({
          message: props.location.state.message,
          buttonDestination: props.location.state.buttonDestination,
          buttonText: props.location.state.buttonText,
          isError: false
        });
      }

  }, []);

  return (
    <Container
      maxWidth="md">
      <StyledPaper>
        <Typography variant="h5" component="h3">
          {info.message}
        </Typography>
        <Button
          type="button"
          fullWidth
          variant="contained"
          color="primary"
          href={info.buttonDestination}
        >
          {info.buttonText}
        </Button>
      </StyledPaper>
    </Container>
  )
}

export default InfoScreen;