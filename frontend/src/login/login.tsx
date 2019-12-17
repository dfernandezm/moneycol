import React, { useState } from "react";
import { connect, ConnectedProps } from "react-redux";
import { Redirect } from "react-router-dom";
import { loginUser } from "../actions";
import { withStyles } from "@material-ui/styles";

import Avatar from "@material-ui/core/Avatar";
import Button from "@material-ui/core/Button";
import TextField from "@material-ui/core/TextField";
import LockOutlinedIcon from "@material-ui/icons/LockOutlined";
import Typography from "@material-ui/core/Typography";
import Paper from "@material-ui/core/Paper";
import Container from "@material-ui/core/Container";

import {RootState} from '../reducers'

const styles: any = () => ({
  "@global": {
    body: {
      backgroundColor: "#fff"
    }
  },
  paper: {
    marginTop: 100,
    display: "flex",
    padding: 20,
    flexDirection: "column",
    alignItems: "center"
  },
  avatar: {
    marginLeft: "auto",
    marginRight: "auto",
    backgroundColor: "#f50057"
  },
  form: {
    marginTop: 1
  },
  errorText: {
    color: "#f50057",
    marginBottom: 5,
    textAlign: "center"
  }
});

const Login: React.FC = (props:any) => {
  const [state, setState] = useState({ email: "", password: "" });
    
  const handleEmailChange = (event: React.ChangeEvent<HTMLInputElement>) => {
    setState({...state, email: event.target.value });
  };

  const handlePasswordChange = (event: React.ChangeEvent<HTMLInputElement>) => {
    setState({...state, password: event.target.value });
  };

  const handleSubmit = () => {
    const { dispatch } = props;
    const { email, password } = state;

    dispatch(loginUser(email, password));
  };

  const { classes, loginError, isAuthenticated } = props;

  if (isAuthenticated) {
    return <Redirect to="/protected" />;
  } else {
      return (
        <Container component="main" maxWidth="xs">
          <Paper className={classes.paper}>
            <Avatar className={classes.avatar}>
              <LockOutlinedIcon />
            </Avatar>
            <Typography component="h1" variant="h5">
              Sign in
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
            {loginError && (
              <Typography component="p" className={classes.errorText}>
                Incorrect email or password.
              </Typography>
            )}
            <Button
              type="button"
              fullWidth
              variant="contained"
              color="primary"
              className={classes.submit}
              onClick={handleSubmit}
            >
              Sign In
            </Button>
          </Paper>
        </Container>
      );
    }
}

//https://redux.js.org/recipes/usage-with-typescript
const mapState = (state: RootState) => {
  return {
    isLoggingIn: state.auth.isLoggingIn,
    loginError: state.auth.loginError,
    isAuthenticated: state.auth.isAuthenticated
  };
}

// empty
const mapDispatch = {}

const connector = connect(
    mapState,
    mapDispatch
  )

//TODO: to remove the 'any' in Props
//   type PropsFromRedux = ConnectedProps<typeof connector>
//   type Props = PropsFromRedux & {
//     backgroundColor: string
//   }

export default withStyles(styles)(connector(Login));