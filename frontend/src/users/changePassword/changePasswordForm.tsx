import React from "react";

import TextField from "@material-ui/core/TextField";
import Button from "@material-ui/core/Button";

import { Formik, Form, FormikHelpers, FormikProps } from 'formik';

import { useMutation } from "@apollo/react-hooks";
import { CHANGE_PASSWORD_MUTATION } from "./gql/changePassword";
import { localStateService } from '../../login/localState/localStateService'
import { RouteComponentProps, withRouter } from "react-router-dom";

interface ChangePasswordData {
    oldPassword: string,
    newPassword: string,
    newPasswordRepeated: string
}

// Formik Values
type Values = Omit<ChangePasswordData, 'email'>;

/**
 * The form where authenticated users can change their password if they have signed up with email/password. This is a protected route, therefore
 * users will be prompted to login if accessed directly
 * 
 */
const ChangePasswordForm: React.FC<RouteComponentProps> = (props: RouteComponentProps) => {

    const [changePassword] = useMutation(CHANGE_PASSWORD_MUTATION);

    const initialValues: Values = {
        oldPassword: '',
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
        if (!values.oldPassword) {
            errors.oldPassword = 'Required';
        }
        if (!values.newPassword) {
            errors.oldPassword = 'Required';
        }
        if (!values.newPasswordRepeated) {
            errors.oldPassword = 'Required';
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

            const changePasswordInput = { ...values, email: localStateService.getUser()?.email };
            const { data } = await changePassword({ variables: { changePasswordInput: changePasswordInput } });

            console.log("changed Password", data.changePassword);

            setSubmitting(false);

            //TODO: should invalidate current session (maybe /logout route)

            props.history.replace("/login");

        } catch (err) {
            // setSubmitting call is repeated here instead of in a finally block otherwise React complains
            // about changing state in an unmounted component
            setSubmitting(false)
            console.log(err);

            // TODO: this error gives too much information, create a function to give meaningful error messages
            setStatus("Error updating user profile: " + err.message);
        }
    }

    return (
        <Formik
            initialValues={initialValues}
            validate={performValidation}
            onSubmit={handleSubmit}
            validateOnChange={false}
            component={InnerUserProfileForm} />
    );
}

const InnerUserProfileForm = ({
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
                id="oldPassword"
                label="Old password"
                name="oldPassword"
                type="password"
                onChange={handleChange}
                value={values.oldPassword}
            />

            { //TODO: component for errors
                // or style for errors: https://material-ui.com/components/text-fields/ 
                errors.oldPassword ? <div>{errors.oldPassword}</div> : null
            }

            <TextField
                variant="outlined"
                margin="normal"
                fullWidth
                name="newPassword"
                label="New password"
                id="newPassword"
                type="password"
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
                type="password"
                onChange={handleChange}
                value={values.newPasswordRepeated}
            />

            { //TODO: component for errors
                // or style for errors: https://material-ui.com/components/text-fields/ 
                errors.newPasswordRepeated ? <div>{errors.newPasswordRepeated}</div> : null
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

export default withRouter(ChangePasswordForm);

