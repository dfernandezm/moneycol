import React, { useState, useEffect } from "react";

import TextField from "@material-ui/core/TextField";
import Button from "@material-ui/core/Button";

import { Formik, Form, FormikHelpers, FormikProps } from 'formik';

import { useMutation } from "@apollo/react-hooks";
import { COMPLETE_PASSWORD_RESET } from "./gql/changePassword";
import { RouteComponentProps, withRouter } from "react-router-dom";

import queryString from 'query-string';
import ErrorMessage from "../errors/errorMessage";

interface ResetPasswordParams {
    code: string,
    continueUrl: string,
    lang: string
}

interface CompletePasswordResetData {
    email: string,
    code: string,
    newPassword: string
}

interface CompleteResetPasswordExtra {
    newPasswordRepeated: string
}

// Formik Values
type Values = Omit<CompletePasswordResetData, 'code'> & CompleteResetPasswordExtra;

const asString = (value: any): string => {
    return value + "" || "" as string;
}

const parseQueryParams = (searchLocation: string): ResetPasswordParams => {
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

/**
 * The form where a reset password request is completed by sending the current email and a new password to be set.
 * Users land here after clicking in a link present the request password reset email.
 */
const CompletePasswordResetForm: React.FC<RouteComponentProps> = (props: RouteComponentProps) => {

    const [completePasswordReset] = useMutation(COMPLETE_PASSWORD_RESET);
    const [passwordResetCode, setPasswordResetCode] = useState('');
    const [codeError, setCodeError] = useState(false);

    useEffect(() => {
        try {
            const completePasswordResetParams = parseQueryParams(props.location.search);
            console.log("Verify email params", completePasswordResetParams);

            // it could validate the code here, but it's done at the same time the new password is set
            setPasswordResetCode(completePasswordResetParams.code);
        } catch (err) {
            console.log("Error parsing parameters", err);
            setCodeError(true);
        }
    }, [props.location.search]);
    
    const initialValues: Values = {
        email: '',
        newPassword: '',
        newPasswordRepeated: ''
    };

    /**
     * Formik validation handler
     * 
     * It should use Yup for more powerful validation
     * 
     * @param values The form values
     */
    const performValidation = (values: Values) => {
        const errors: Partial<Values> = {};

        if (!values.email) {
            errors.email = 'Required';
        }
        if (!values.newPassword) {
            errors.newPassword = 'Required';
        }
        if (!values.newPasswordRepeated) {
            errors.newPasswordRepeated = 'Required';
        }
        if (values.newPassword !== values.newPasswordRepeated) {
            errors.newPasswordRepeated = "Passwords don't match";
        }

        return errors;
    }

    /**
     * Formik submission handler.  
     * 
     * @param values {Values} the form values to submit
     * @param formikHelpers {FormikHelpers<Values>} in this case <code>setStatus</code> to set the form status after successful / failed submission
     * and <code>setSubmitting</code> to change submission state both on error or success
     */
    const handleSubmit = async (values: Values, { setStatus, setSubmitting }: FormikHelpers<Values>) => {
        try {

            const { data } = await completePasswordReset({ variables: { 
                email: values.email, 
                code: passwordResetCode,
                newPassword: values.newPassword } 
            });

            console.log("Password reset completed", data.completePasswordReset);

            setSubmitting(false);
            props.history.replace("/login");

        } catch (err) {
            // setSubmitting call is repeated here instead of in a finally block otherwise React complains
            // about changing state in an unmounted component
            setSubmitting(false)
            console.log(err);

            // TODO: this error gives too much information, create a function to give meaningful error messages
            setStatus("Error completing password reset: " + err.message);
        }
    }

    return (
        <>
            { 
                codeError ?
                //TODO: should have a button to resend the email
                <ErrorMessage errorMessage="Invalid verification code: need to verify again"/>
                :
                <Formik
                    initialValues={initialValues}
                    validate={performValidation}
                    onSubmit={handleSubmit}
                    validateOnChange={false}
                    component={InnerForm} 
                />
            }
        </>
    );
}

const InnerForm = ({
    handleSubmit,
    handleChange,
    values,
    errors,
    isSubmitting,
    status }: FormikProps<Values>) => (

        <Form onSubmit={handleSubmit}>

            <TextField
                variant="outlined"
                margin="normal"
                fullWidth
                id="email"
                label="Email"
                name="email"
                onChange={handleChange}
                value={values.email}
            />

            { //TODO: component for errors
                // or style for errors: https://material-ui.com/components/text-fields/ 
                errors.email ? <div>{errors.email}</div> : null
            }

            <TextField
                variant="outlined"
                margin="normal"
                fullWidth
                name="newPassword"
                label="New password"
                id="newPassword"
                onChange={handleChange}
                value={values.newPassword}
            />

            <TextField
                variant="outlined"
                margin="normal"
                fullWidth
                name="newPasswordRepeated"
                label="Repeat new password"
                id="newPasswordRepeated"
                onChange={handleChange}
                value={values.newPasswordRepeated}
            />

            <Button
                type="submit"
                fullWidth
                variant="contained"
                color="primary"
                disabled={isSubmitting}
            >
                {isSubmitting ? "Submitting" : "Submit"}
            </Button>
            {!!status && <div> {status} </div>}
        </Form>
    )

export default withRouter(CompletePasswordResetForm);

