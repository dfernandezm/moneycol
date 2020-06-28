import React from "react";
import Container from "@material-ui/core/Container";
import Typography from "@material-ui/core/Typography";
import { styled } from '@material-ui/core/styles';
import Paper from "@material-ui/core/Paper";

const StyledPaper = styled(Paper)({
    marginTop: 100,
    display: "flex",
    padding: 20,
    flexDirection: "column",
    alignItems: "center",
    color: "red"
});

interface ErrorProps {
    errorMessage: string
}

const ErrorMessage: React.FC<ErrorProps> = ({ errorMessage = "Error" }) => {
    return (
      <Container
        maxWidth="md">
        <StyledPaper>
          <Typography variant="h5" component="h3">
            {errorMessage}
          </Typography>
        </StyledPaper>
      </Container>
    )
  }

  export default ErrorMessage;