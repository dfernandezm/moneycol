import React from "react";
import Container from "@material-ui/core/Container";
import CircularProgress from '@material-ui/core/CircularProgress';
import { styled } from '@material-ui/core/styles';
import Paper from "@material-ui/core/Paper";

const StyledPaper = styled(Paper)({
    marginTop: 100,
    display: "flex",
    padding: 20,
    flexDirection: "column",
    alignItems: "center"
});

interface LoadingProps {
    loadingMessage: string
}

const Loading: React.FC<LoadingProps> = ({ loadingMessage = "Loading" }) => {
    return (
      <Container
        maxWidth="md">
        <StyledPaper>
            <CircularProgress />
        </StyledPaper>
      </Container>
    )
  }

  export default Loading;