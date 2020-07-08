import React from "react";

import TextField from "@material-ui/core/TextField";
import Button from "@material-ui/core/Button";

import { Formik, Form, FormikHelpers, FormikProps } from 'formik';

import { useMutation } from "@apollo/react-hooks";
import { UPDATE_USER_PROFILE_MUTATION } from "./gql/updateUserProfile";

import { RouteComponentProps, withRouter } from "react-router-dom";
import { UpdateQueryOptions } from "apollo-boost";

interface UserProfileData {
  userId: string, // TODO: should be passed here or separate prop or fetch inside
  username: string,
  firstName?: string,
  lastName?: string
}

// Formik Values
type Values = Omit<UserProfileData, 'userId'>;

interface UserProfileProps {
  initialData: UserProfileData
}

/**
 * The form where users can update their profile information. This is a protected route, therefore
 * users will be prompted to login if accessed directly
 * 
 */
const UpdateUserProfileForm: React.FC<RouteComponentProps & UserProfileProps> = (props: RouteComponentProps & UserProfileProps) => {

  const [updateUserProfile] = useMutation(UPDATE_USER_PROFILE_MUTATION);

  const initialValues: Values = {
    username: props.initialData.username,
    firstName: props.initialData.firstName,
    lastName: props.initialData.lastName
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
    if (!values.username) {
      errors.username = 'Required';
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

      const userInput = { ...values, userId: props.initialData.userId };
      const { data } = await updateUserProfile({ variables: { userInput: userInput } });

      console.log("Updated user", data.updateUserProfile);

      setSubmitting(false);
      props.history.replace("/protected");

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
        id="username"
        label="Username"
        name="username"
        onChange={handleChange}
        value={values.username}
      />

      { //TODO: component for errors
        // or style for errors: https://material-ui.com/components/text-fields/ 
        errors.username ? <div>{errors.username}</div> : null
      }

      <TextField
        variant="outlined"
        margin="normal"
        fullWidth
        name="firstName"
        label="First name"
        id="firstName"
        onChange={handleChange}
        value={values.firstName}
      />

      <TextField
        variant="outlined"
        margin="normal"
        fullWidth
        name="lastName"
        label="Last name"
        id="lastName"
        onChange={handleChange}
        value={values.lastName}
      />

      <Button
        type="submit"
        fullWidth
        variant="contained"
        color="primary"
        disabled={isSubmitting}
      >
        {isSubmitting ? "Updating" : "Update"}
      </Button>
      { !!status && <div> {status} </div> }
    </Form>
  )

export default withRouter(UpdateUserProfileForm);
