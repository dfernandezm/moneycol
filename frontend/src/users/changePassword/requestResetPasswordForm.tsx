import React, { useState } from "react";

import TextField from "@material-ui/core/TextField";
import Button from "@material-ui/core/Button";

import { Formik, Form, FormikHelpers, FormikProps } from 'formik';

import { useMutation } from "@apollo/react-hooks";
import { REQUEST_PASSWORD_RESET } from "./gql/changePassword";
import { RouteComponentProps, withRouter, Redirect } from "react-router-dom";

interface ResetPasswordData {
    email: string,
}

// Formik Values
type Values = ResetPasswordData;

/**
 * The form where non-authenticated users can request a password reset
 * 
 */
const ResetPasswordForm: React.FC<RouteComponentProps> = (props: RouteComponentProps) => {

    const [requestPasswordReset] = useMutation(REQUEST_PASSWORD_RESET);
    const [requested, setRequested] = useState(false);
    const initialValues: Values = {
        email: ''
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

        //TODO: validate email
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
            
            const { data } = await requestPasswordReset({ variables: { email: values.email } });

            console.log("requested password reset", data.requestPasswordReset);

            //TODO: redirect to new page with instructions - visit the link in your email to change your password
            setSubmitting(false);
            setRequested(true);

            } catch (err) {
                // setSubmitting call is repeated here instead of in a finally block otherwise React complains
                // about changing state in an unmounted component
                setSubmitting(false)
                console.log(err);

                // TODO: this error gives too much information, create a function to give meaningful error messages
                setStatus("Error requesting password reset: " + err.message);
            }
        }

    return (
            <> { 
                !requested ? 
                
                <Formik
                initialValues={initialValues}
                validate={performValidation}
                onSubmit={handleSubmit}
                validateOnChange={false}
                component={InnerForm} /> 
                :
                <Redirect to={{
                    pathname: '/users/info', 
                    state: {
                        buttonText: "Resend email", 
                        buttonDestination: "/users/resetPassword",
                        message: "Check your email. A link to reset your password has been sent"
                    }
                }}/>
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

    export default withRouter(ResetPasswordForm);

