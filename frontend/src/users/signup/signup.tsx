import React from "react";

import { StyledPaper } from '../styles/sharedStyles';
import { Typography } from "@material-ui/core";
import Container from "@material-ui/core/Container";

import SignupForm  from "./signupForm";

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