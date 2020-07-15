import React, { useEffect, useState } from "react";
import queryString from 'query-string';

import { RouteComponentProps } from "react-router-dom";
import { useMutation } from '@apollo/react-hooks';
import { VERIFY_EMAIL_GQL } from './gql/verifyEmail';
import Loading from "./loading";
import ErrorMessage from "../errors/errorMessage";
import EmailVerified from "./emailVerified";

type EmailVerificationParameters = {
    code: string,
    continueUrl: string,
    lang: string
}

enum ValidationResult {
    PENDING = "PENDING",
    SUCCESS = "SUCCESS",
    FAILED = "FAILED"
}

const asString = (value: any): string => {
    return value + "" || "" as string;
}

const parseVerifyEmailParams = (searchLocation: string): EmailVerificationParameters => {
    console.log("Search string: " + searchLocation);
    const queryStringValues = queryString.parse(searchLocation);
    
    if (!queryStringValues.oobCode) {
        throw new Error("Invalid code: " + queryStringValues);
    }

    let code = asString(queryStringValues.oobCode);
    const continueUrl = asString(queryStringValues.continueUrl);
    const lang = asString(queryStringValues.lang);

    if (!code) {
        throw new Error("Invalid code: " + code);
    }

    return {
        code, continueUrl, lang
    }
}

const onError = (err: Error) => {
    console.log("Error verifying email", err);
    throw err;
}

/**
 * This component is redirected to from a 'verify' handler,  received as action link in the email
 * sent to an user after signup in order to verify their email address.
 * 
 * The properties for verification are received in the querystring:
 * - oobCode: the generated code in the email link and used for verification
 * - continueUrl: url to redirect to after verification is complete
 * - lang: the language of the email template
 * 
 */
const VerifyEmail: React.FC<RouteComponentProps> = (props: RouteComponentProps) => {

    const [validationResult, setValidationResult] = useState(ValidationResult.PENDING);
    const [verifyEmail, { data, loading, error, called }] = useMutation(VERIFY_EMAIL_GQL);

    useEffect(() => {

        // This is a inner async function needed to allow useEffect to run promises/async/await. If it's outside,
        // the error is not properly thrown and caught, this should be investigated further.
        // See: https://stackoverflow.com/questions/59465864/handling-errors-with-react-apollo-usemutation-hook
        const verifyEmailCall = async (verifyEmailParams: EmailVerificationParameters, verifyEmail: Function) => {
            console.log("verifyParams to call mutation", verifyEmailParams);
            const verifyEmailInput = {
                code: verifyEmailParams.code,
                comebackUrl: verifyEmailParams.continueUrl,
                lang: verifyEmailParams.lang
            }

            try {
                const verifyEmailResult = await verifyEmail({ variables: { verifyEmailInput: verifyEmailInput }, 
                                                              onError: onError });
                return verifyEmailResult; 
            } catch (err) {
                console.log("Error validating email", err);
                setValidationResult(ValidationResult.FAILED);
            }
        }

        try {
            if (!props.location || !props.location.search) {
                setValidationResult(ValidationResult.SUCCESS);
            } else {
                const verifyEmailParams = parseVerifyEmailParams(props.location.search);
                console.log("Verify email params", verifyEmailParams);
                const verifyEmailResult = verifyEmailCall(verifyEmailParams, verifyEmail);
                console.log("Verify email result", verifyEmailResult);
                setValidationResult(ValidationResult.SUCCESS);
            }
        } catch (err) {
            console.log("Error validating email", err);
            setValidationResult(ValidationResult.FAILED);
        }
    }, [props.location, props.location.search, verifyEmail]);

    return (
        <>
            {
                ((validationResult === ValidationResult.SUCCESS) &&
                    <EmailVerified
                        message="Email successfully verified" 
                        buttonText="Continue" 
                        />) ||
                
                    (((!called || loading) && validationResult === ValidationResult.PENDING) && 
                        <Loading loadingMessage="Loading" /> ) ||

                    //TODO: in this case, need to add a button to send a new verification email
                    // This requires backend operation to send a verification email to a given address    
                    ((validationResult === ValidationResult.FAILED || error) && 
                        <ErrorMessage errorMessage="Email verification not successful: need to verify again"/>)
            }
        </>
    );
}

export default VerifyEmail;
