import React, { useState } from "react";
import { connect, useDispatch } from "react-redux";
import { Redirect } from "react-router-dom";
import { loginUser, loginUserWithGoogle } from "./actions";
import { styled, makeStyles } from '@material-ui/core/styles';

import { useApolloClient } from '@apollo/react-hooks';

import Avatar from "@material-ui/core/Avatar";
import Button from "@material-ui/core/Button";
import TextField from "@material-ui/core/TextField";
import LockOutlinedIcon from "@material-ui/icons/LockOutlined";
import Typography from "@material-ui/core/Typography";
import Paper from "@material-ui/core/Paper";
import Container from "@material-ui/core/Container";

import { RootState } from './reducers'
import { Link } from "@material-ui/core";
import { Link as RouterLink } from 'react-router-dom';
import GoogleLoginScreen from "./google/googleLogin";
import Loading from "../users/verifyEmail/loading";

const useStyles = makeStyles({
  form: {
    marginTop: 1
  },
  errorText: {
    color: "#f50057",
    marginBottom: 5,
    textAlign: "center"
  },
  "@global": {
    body: {
      backgroundColor: "#fff"
    }
  }
});

const StyledPaper = styled(Paper)({
    marginTop: 100,
    display: "flex",
    padding: 20,
    flexDirection: "column",
    alignItems: "center"
});

const StyledAvatar = styled(Avatar)({
  marginLeft: "auto",
  marginRight: "auto",
  backgroundColor: "#f50057"
});

interface LoginProps {
  loginError: boolean
  isLoggingIn: boolean
}

const Login: React.FC<LoginProps> = (props: LoginProps) => {

  const [state, setState] = useState({ email: "", password: "" });
  const [loginSuccess, setLoginSuccess] = useState(false);
  const [provider, setProvider] = useState("PASSWORD");
  const [isLoggingWithGoogle, setIsLoggingWithGoogle] = useState(false);
  const dispatch = useDispatch();
  const apolloClient = useApolloClient();
  const classes = useStyles();

  const handleEmailChange = (event: React.ChangeEvent<HTMLInputElement>) => {
    setState({ ...state, email: event.target.value });
  };

  const handlePasswordChange = (event: React.ChangeEvent<HTMLInputElement>) => {
    setState({ ...state, password: event.target.value });
  };

  // See for alternative: https://scotch.io/courses/getting-started-with-react-and-redux/dispatching-on-click
  const handleSubmit = async () => {
    setProvider("PASSWORD");
    const { email, password } = state;
    const loginDispatcher = loginUser(email, password);
    const result = await loginDispatcher(dispatch, {}, apolloClient);
    setLoginSuccess(result === "success");
  };

  const handleSuccessfulGooglePreLogin = async (idToken: string): Promise<void> => {
    setProvider("GOOGLE");
    setIsLoggingWithGoogle(true);
    const loginDispatcher = loginUserWithGoogle(idToken);
    const result = await loginDispatcher(dispatch, {}, apolloClient);
    setLoginSuccess(result === "success");
  }

  //TODO: check in other sites if setting this error is required
  // Failure to open Google or login with it
  const handleErrorOnGooglePreLogin = async (errorResponse: object) => {
    setProvider("GOOGLE");
    console.log("Google login error: ", errorResponse);
  }

  const { loginError, isLoggingIn } = props;

  if (loginSuccess) {
    return <Redirect to="/protected" />;
  } else if (isLoggingWithGoogle) {
    return <Loading />;
  } else {
    return (
      <Container component="main" maxWidth="xs">
        <StyledPaper>
          <StyledAvatar>
            <LockOutlinedIcon />
          </StyledAvatar>
          <Typography component="h1" variant="h5">
          { isLoggingIn ? "Signing In" : "Sign in" }
          </Typography>
          <TextField
            variant="outlined"
            margin="normal"
            fullWidth
            id="email"
            label="Email Address"
            name="email"
            onChange={handleEmailChange}
          />
          <TextField
            variant="outlined"
            margin="normal"
            fullWidth
            name="password"
            label="Password"
            type="password"
            id="password"
            onChange={handlePasswordChange}
          />
          {
          
          (loginError && provider === "PASSWORD" && (
            <Typography component="p" className={classes.errorText}>
              Incorrect email or password.
            </Typography>))
          }
          <Button
            type="button"
            fullWidth
            variant="contained"
            color="primary"
            disabled={isLoggingIn}
            onClick={handleSubmit}>
            { isLoggingIn ? "Signing In" : "Sign in" }
          </Button>
          <GoogleLoginScreen
            onSuccess={handleSuccessfulGooglePreLogin}
            onFailure={handleErrorOnGooglePreLogin}
            />
          {
              (loginError && provider === "GOOGLE" && (
              <Typography component="p" className={classes.errorText}>
                Error occurred during login with Google
              </Typography>))
          }
          <Typography component="p">
            <Link component={RouterLink} to="/users/resetPassword">
              Forgotten password?
            </Link>
          </Typography>
        </StyledPaper>
      </Container>
    );
  }
}

// https://redux.js.org/recipes/usage-with-typescript
const mapState = (state: RootState) => {
  return {
    isLoggingIn: state.auth.isLoggingIn,
    loginError: state.auth.loginError
  };
}

// empty
const mapDispatch = {}

const connector = connect(
  mapState,
  mapDispatch
)

export default connector(Login);