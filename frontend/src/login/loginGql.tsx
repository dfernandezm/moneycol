import React, { useState } from "react";
// import { connect, useDispatch } from "react-redux";
import { useMutation } from '@apollo/react-hooks';
import { LOGIN_GQL } from './gql/login';
import { Redirect } from "react-router-dom";
import { withStyles, createStyles } from "@material-ui/styles";

import Avatar from "@material-ui/core/Avatar";
import Button from "@material-ui/core/Button";
import TextField from "@material-ui/core/TextField";
import LockOutlinedIcon from "@material-ui/icons/LockOutlined";
import Typography from "@material-ui/core/Typography";
import Paper from "@material-ui/core/Paper";
import Container from "@material-ui/core/Container";

// import { RootState } from './reducers'

const styles = createStyles({
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

interface LoginProps {
  classes: any //TODO: investigate what's the type of this
  isAuthenticated: boolean
}

//https://www.apollographql.com/docs/react/data/mutations/#usemutation-api

const LoginGql: React.FC<LoginProps> = (props: LoginProps) => {

  const { classes, isAuthenticated } = props;
  const [state, setState] = useState({ email: "", password: "" });
  const [mutationError, setMutationError] = useState(null);
  const [loggedIn, setLoggedIn] = useState(isAuthenticated);


  // TODO: handle errors network errors (rejection)
  // https://stackoverflow.com/questions/59465864/handling-errors-with-react-apollo-usemutation-hook
  const [ loginUser, { loading: isLoggingIn, error: loginError }] = useMutation(LOGIN_GQL);

  const handleEmailChange = (event: React.ChangeEvent<HTMLInputElement>) => {
    setState({ ...state, email: event.target.value });
  };

  const handlePasswordChange = (event: React.ChangeEvent<HTMLInputElement>) => {
    setState({ ...state, password: event.target.value });
  };

  const handleSubmit = async () => {
    const { email, password } = state;
    try {
      const { data: { loginWithEmail } } = await loginUser({ variables: { email, password } })
      console.log("loginwithemail", loginWithEmail);
      localStorage.setItem("token", loginWithEmail.token);
      console.log("Writing token: ", loginWithEmail.token);
      setLoggedIn(true);
    } catch (err) {
      console.log("Error login: ", err);
      setMutationError(err);
    }
  };

  if (loggedIn) {
    console.log("Going to protected");
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
          {(loginError || mutationError) && (
            <Typography component="p" className={classes.errorText}>
              Incorrect email or password.
              </Typography>
          )}
           {isLoggingIn ? (
            <Button
            type="button"
            fullWidth
            variant="contained"
            color="primary"
            className={classes.submit}
            disabled={true}
            onClick={handleSubmit}>
            Signing In...
            </Button>
          ) : <Button
                type="button"
                fullWidth
                variant="contained"
                color="primary"
                disabled={false}
                className={classes.submit}
                onClick={handleSubmit}>
                Sign In
            </Button>
          }
        </Paper>
      </Container>
    );
  }
}

//https://redux.js.org/recipes/usage-with-typescript
// const mapState = (state: RootState) => {
//   return {
//     isLoggingIn: state.auth.isLoggingIn,
//     loginError: state.auth.loginError,
//     isAuthenticated: state.auth.isAuthenticated
//   };
// }

// empty
// const mapDispatch = {}

// const connector = connect(
//   mapState,
//   mapDispatch
// )

export default withStyles(styles)(LoginGql);