import React from "react";

import { StyledPaper } from '../styles/sharedStyles';
import { Typography } from "@material-ui/core";
import Container from "@material-ui/core/Container";

import ChangePasswordForm  from "./changePasswordForm";

const ChangePassword: React.FC<{}> = () => {
  return (
    <Container component="main" maxWidth="xs">
      <StyledPaper>
        <Typography component="h4" variant="h4">
          Change password
        </Typography>
        <ChangePasswordForm />
      </StyledPaper>
    </Container>
  )
}

export default ChangePassword;
