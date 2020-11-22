import React from 'react';
import { GoogleLogin } from 'react-google-login';

// should be fetched from server or included at build time through .env
const clientId = "461081581931-9il6usq9lq5c5719l0qmcnv4kt510cc7.apps.googleusercontent.com";

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

const GoogleLoginScreen: React.FC<GoogleLoginProps> = ({ onSuccess, onFailure }) => {
  return (
        <GoogleLogin
            clientId={clientId}
            buttonText="Login with Google"
            onSuccess={(response) => successHandler(response, onSuccess)}
            onFailure={(response) => errorHandler(response, onFailure)}
            cookiePolicy={'single_host_origin'}
        />
  )
}

export default GoogleLoginScreen;


