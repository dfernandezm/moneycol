import React from 'react';
import { GoogleLogin } from 'react-google-login';

const successHandler = async (response:any, onSuccess: Function) => {
    console.log("Successful Google login");
    const idToken = response.tokenId;
    await onSuccess(idToken);
}

const errorHandler = async (response:any, onFailure: Function) => {
    console.log("Failure",response);
    await onFailure(response);
}

interface GoogleLoginProps {
    onSuccess: (idToken: string) => Promise<void>
    onFailure: (failureResponse: any) => object
}

const GoogleLoginScreen: React.FC<GoogleLoginProps> = (props: GoogleLoginProps) => {
  return (
        <GoogleLogin
            // Remove this clientId from here?
            clientId="461081581931-9il6usq9lq5c5719l0qmcnv4kt510cc7.apps.googleusercontent.com"
            buttonText="Login with Google"
            onSuccess={(response) => successHandler(response, props.onSuccess)}
            onFailure={(response) => errorHandler(response, props.onFailure)}
            cookiePolicy={'single_host_origin'}
        />
  )
}

export default GoogleLoginScreen;


