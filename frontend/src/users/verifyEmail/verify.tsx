import React, { useState, useEffect } from "react";

import queryString from 'query-string';
import ErrorMessage from "../errors/errorMessage";
import { RouteComponentProps, Redirect } from "react-router-dom";
// import CreateNewPassword from '../changePassword/createNewPassword';
// import VerifyEmail from './verifyEmail';

interface VerifyParams {
    mode: string,
    code: string,
    continueUrl: string,
    lang: string
}

const asString = (value: any): string => {
    return value + "" || "" as string;
}

const parseVerifyParams = (searchLocation: string): VerifyParams => {
    console.log("Search string: " + searchLocation);
    const queryStringValues = queryString.parse(searchLocation);
    
    if (!queryStringValues.oobCode) {
        throw new Error("Invalid code: " + queryStringValues);
    }

    if (!queryStringValues.mode) {
        throw new Error("Invalid mode: " + queryStringValues);
    } 

    let mode = asString(queryStringValues.mode);
    let code = asString(queryStringValues.oobCode);
    const continueUrl = asString(queryStringValues.continueUrl);
    const lang = asString(queryStringValues.lang);

    if (!code) {
        throw new Error("Invalid code: " + code);
    }

    return {
        mode, code, continueUrl, lang
    }
}

const Verify: React.FC<RouteComponentProps> = (props: RouteComponentProps) => {

    const [verifyParams, setVerifyParams] = useState<VerifyParams>();
    const [errorMessage, setErrorMessage] = useState('');
    useEffect(() => {

        try {
            const verifyParams = parseVerifyParams(props.location.search);
            console.log("Verify params", verifyParams);
            setVerifyParams(verifyParams);
        } catch (err) {
            console.log("Error validating email", err);
            setErrorMessage("Error validating: " + err.message);
        }

    }, []);

    return (
        <>
        {
            ((verifyParams?.mode === "resetPassword") && 
            
            <Redirect to={
                {
                    
                    pathname: "/users/completePasswordReset",
                    search: "?oobCode=" + verifyParams.code + 
                            "&continueUrl=" + verifyParams.continueUrl +
                            "&lang=" + verifyParams.lang,
                }
            } />) ||
            
            ((verifyParams?.mode === "verifyEmail") && 
            <Redirect to={
                {
                    
                    pathname: "/users/verifyEmail",
                    search: "?oobCode=" + verifyParams.code + 
                            "&continueUrl=" + verifyParams.continueUrl +
                            "&lang=" + verifyParams.lang,
                }
            } />) || 

            <ErrorMessage errorMessage={errorMessage} />
            
            }
        </>
    );
}

export default Verify;