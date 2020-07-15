import React from "react";

import { StyledPaper } from '../styles/sharedStyles';
import { Typography } from "@material-ui/core";
import Container from "@material-ui/core/Container";

import CompletePasswordResetForm  from "./completePasswordResetForm";
import { RouteComponentProps } from "react-router-dom";

const CreateNewPassword: React.FC<RouteComponentProps> = (props: RouteComponentProps) => {
  return (
    <Container component="main" maxWidth="xs">
      <StyledPaper>
        <Typography component="h4" variant="h4">
          Create a new password
        </Typography>
        <CompletePasswordResetForm {...props} />
      </StyledPaper>
    </Container>
  )
}

export default CreateNewPassword;
