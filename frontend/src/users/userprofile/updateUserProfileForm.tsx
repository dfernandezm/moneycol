import React from "react";

import TextField from "@material-ui/core/TextField";
import Button from "@material-ui/core/Button";

import { Formik, Form, FormikHelpers, FormikProps } from 'formik';

import { useMutation, useQuery } from "@apollo/react-hooks";
import { UPDATE_USER_PROFILE_MUTATION } from "./gql/updateUserProfile";
import { FIND_USER_PROFILE_QUERY } from "./gql/findUserProfile";

import { RouteComponentProps, withRouter } from "react-router-dom";

import { localStateService } from '../../login/localState/localStateService'

// Formik form values
interface Values {
  username: string | '' | undefined;
  firstName: string | '' | undefined;
  lastName: string | '' | undefined
}

/**
 * The form where users can update their profile information. This is a protected route, therefore
 * users will be prompted to login if accessed directly
 * 
 */
const UpdateUserProfileForm: React.FC<RouteComponentProps & Values> = (props: RouteComponentProps & Values) => {

  //TODO: We need to pre-fill the form with user profile data,
  // we may just store in localState the minimum (userId, email)
  // and do a query to GQL to get the full profile to update
  const user = localStateService.getUser();
  const [updateUserProfile] = useMutation(UPDATE_USER_PROFILE_MUTATION);

  // const { data, loading, error } = useQuery(FIND_USER_PROFILE_QUERY, {
  //   variables: { userId: user?.userId },
  // });

  const initialValues: Values = {
    username: props.username,
    firstName: props.firstName,
    lastName: props.lastName
  };

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

      //TODO: pass the userId from the stored user properly (not optional)
      const userInput = { ...values, userId: user?.userId };
      const { data } = await updateUserProfile({ variables: { userInput: userInput } });

      console.log("Updated user", data.updateUserProfile);

      setSubmitting(false);
      props.history.replace("/protected");

    } catch (err) {
      // Repeat setSubmitting call instead of finally block otherwise React complains
      // about changing state in an unmounted component
      setSubmitting(false)
      console.log(err);
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
      {!!status && <div> {status} </div>}
    </Form>
  )

export default withRouter(UpdateUserProfileForm);
