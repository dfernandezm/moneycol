import React from "react";

import { StyledPaper } from '../styles/sharedStyles';
import { Typography } from "@material-ui/core";
import Container from "@material-ui/core/Container";

import UpdateUserProfileForm  from "./updateUserProfileForm";

const UpdateUserProfile: React.FC<{}> = () => {
  return (
    <Container component="main" maxWidth="xs">
      <StyledPaper>
        <Typography component="h4" variant="h4">
          Update profile
        </Typography>
        <UpdateUserProfileForm />
      </StyledPaper>
    </Container>
  )
}

export default UpdateUserProfile;