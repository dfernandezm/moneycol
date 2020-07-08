import React from "react";

import { StyledPaper } from '../styles/sharedStyles';
import { Typography } from "@material-ui/core";
import Container from "@material-ui/core/Container";

import ResetPasswordForm  from "./requestResetPasswordForm";

const ResetPassword: React.FC<{}> = () => {
  return (
    <Container component="main" maxWidth="xs">
      <StyledPaper>
        <Typography component="h4" variant="h4">
          Reset your password
        </Typography>
        <ResetPasswordForm />
      </StyledPaper>
    </Container>
  )
}

export default ResetPassword;
