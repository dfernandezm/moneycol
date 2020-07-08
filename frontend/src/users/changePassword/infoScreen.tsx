import React from "react";
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
}

//TODO: make it more generic so it can receive values from state redirect, or props
type AllInfoProps = InfoProps & RouteComponentProps;

const InfoScreen: React.FC<AllInfoProps> = (props: AllInfoProps) => {
  return (
    <Container
      maxWidth="md">
      <StyledPaper>
        <Typography variant="h5" component="h3">
          {props.location.state.message}
        </Typography>
        <Button
          type="button"
          fullWidth
          variant="contained"
          color="primary"
          href={props.location.state.buttonDestination}
        >
          {props.location.state.buttonText}
        </Button>
      </StyledPaper>
    </Container>
  )
}

export default InfoScreen;