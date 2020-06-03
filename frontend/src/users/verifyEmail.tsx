import React, { useEffect } from "react";
import queryString from 'query-string';

import { styled } from '@material-ui/core/styles';
import Button from "@material-ui/core/Button";
import TextField from "@material-ui/core/TextField";
import Typography from "@material-ui/core/Typography";
import Paper from "@material-ui/core/Paper";
import Container from "@material-ui/core/Container";
import { RouteComponentProps } from "react-router-dom";

const StyledPaper = styled(Paper)({
    marginTop: 100,
    display: "flex",
    padding: 20,
    flexDirection: "column",
    alignItems: "center"
});

type EmailVerificationParameters = {
    code: string,
    continueUrl: string,
    lang: string
}

const parseVerifyEmailParams = (searchLocation: string) => {
    const queryStringValues = queryString.parse(searchLocation);
    const code = queryStringValues.oobCode;
    const continueUrl = queryStringValues.continueUrl
    const lang = queryStringValues.lang
    return {
        code, continueUrl, lang
    }
  }

const VerifyEmail: React.FC<RouteComponentProps> = (props: RouteComponentProps) => {

    useEffect(() => {
      const verifyEmailParams = parseVerifyEmailParams(props.location.search);
      console.log("Verify email params", verifyEmailParams);
    });
  
    return (
      <p>Verify email</p>
    );
  }
  
  export default VerifyEmail;
  