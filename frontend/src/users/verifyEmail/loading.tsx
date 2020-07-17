import React from "react";
import Container from "@material-ui/core/Container";
import CircularProgress from '@material-ui/core/CircularProgress';
import { styled } from '@material-ui/core/styles';
import Paper from "@material-ui/core/Paper";
import { Typography } from "@material-ui/core";

const StyledPaper = styled(Paper)({
    marginTop: 100,
    display: "flex",
    padding: 20,
    flexDirection: "column",
    alignItems: "center"
});

interface LoadingProps {
    loadingMessage?: string
}

const Loading: React.FC<LoadingProps> = ({ loadingMessage }) => {
    return (
      <Container
        maxWidth="md">
        <StyledPaper>
            { loadingMessage && <Typography variant="h6">{loadingMessage}</Typography>}
            <CircularProgress />
        </StyledPaper>
      </Container>
    )
  }

  export default Loading;