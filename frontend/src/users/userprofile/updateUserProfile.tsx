import React from "react";

import { StyledPaper } from '../styles/sharedStyles';
import { Typography } from "@material-ui/core";
import Container from "@material-ui/core/Container";

import { FIND_USER_PROFILE_QUERY } from "./gql/findUserProfile";
import UpdateUserProfileForm  from "./updateUserProfileForm";
import { useQuery } from "@apollo/react-hooks";
import { localStateService } from '../../login/localState/localStateService'

const UpdateUserProfile: React.FC<{}> = () => {
  const user = localStateService.getUser();
  const { data, loading, error } = useQuery(FIND_USER_PROFILE_QUERY, {
    variables: { userId: user?.userId },
  });

  if (loading) return <p>Loading...</p>;
  if (error) return <p>Error: {error}</p>;

  return (
    <Container component="main" maxWidth="xs">
      <StyledPaper>
        <Typography component="h4" variant="h4">
          Update profile
        </Typography>
        {//TODO: set these props in a single object or find a way to do this inside 
        }
        <UpdateUserProfileForm 
          username={data.findUserProfile.username} 
          firstName={data.findUserProfile.firstName}
          lastName={data.findUserProfile.lastName} 
        />
      </StyledPaper>
    </Container>
  )
}

export default UpdateUserProfile;