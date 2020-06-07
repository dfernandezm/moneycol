import React from "react";
import Container from "@material-ui/core/Container";
import Typography from "@material-ui/core/Typography";
import { styled } from '@material-ui/core/styles';
import Paper from "@material-ui/core/Paper";
import { Button } from "@material-ui/core";

const StyledPaper = styled(Paper)({
  marginTop: 100,
  display: "flex",
  padding: 20,
  flexDirection: "column",
  alignItems: "center"
});

//TODO: pass in handler for continueUrl more specific:
// handleContinueAfter: (event: React.MouseEvent<HTMLElement>) => void
interface EmailVerifiedProps {
  message: string,
  buttonText: string,
}

const EmailVerified: React.FC<EmailVerifiedProps> = ({ message, buttonText }) => {
  return (
    <Container
      maxWidth="md">
      <StyledPaper>
        <Typography variant="h5" component="h3">
          {message}
        </Typography>
        <Button
          type="button"
          fullWidth
          variant="contained"
          color="primary"
          href="/login"
          >
          {buttonText}
        </Button>
      </StyledPaper>
    </Container>
  )
}

export default EmailVerified;