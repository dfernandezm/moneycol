import React from "react";
import { styled, makeStyles } from '@material-ui/core/styles';
import { Typography } from "@material-ui/core";
import Paper from "@material-ui/core/Paper";
import Container from "@material-ui/core/Container";
import SignupForm  from "./signupForm";

const StyledPaper = styled(Paper)({
  marginTop: 100,
  display: "flex",
  padding: 20,
  flexDirection: "column",
  alignItems: "center"
});

const Signup: React.FC<{}> = () => {
  return (
    <Container component="main" maxWidth="xs">
      <StyledPaper>
        <Typography component="h4" variant="h4">
          Sign up
        </Typography>
        <SignupForm />
      </StyledPaper>
    </Container>
  )
}

export default Signup;