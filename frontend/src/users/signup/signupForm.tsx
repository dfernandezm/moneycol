import React from "react";
import TextField from "@material-ui/core/TextField";
import Button from "@material-ui/core/Button";
import { Formik, Form, FormikHelpers, FormikProps } from 'formik';
import { useMutation } from "@apollo/react-hooks";
import { SIGNUP_USER_MUTATION } from "./gql/signup";
import { RouteComponentProps, withRouter } from "react-router-dom";

interface Values {
  username: string;
  email: string;
  password: string;
  repeatedPassword: string;
  firstName: string;
  lastName: string
}

/**
 * The Signup form for users with email and password
 * 
 * @see https://medium.com/@kmerandi25/react-form-validation-with-formik-material-ui-and-yup-1cd92eac887
 * @see https://material-ui.com/components/text-fields/#components
 * @see https://stackworx.github.io/formik-material-ui/docs/guide/getting-started
 */
const SignupForm: React.FC<RouteComponentProps> = (props: RouteComponentProps) => {

  const [signupUser] = useMutation(SIGNUP_USER_MUTATION)

  const initialValues: Values = { 
    username: '', 
    email: '', 
    password: '', 
    repeatedPassword: '', 
    firstName: '', 
    lastName: '' 
  };

  const performValidation = (values: Values) => {
    const errors: Partial<Values> = {};
    if (!values.email) {
      errors.email = 'Required';
    } else if (
      !/^[A-Z0-9._%+-]+@[A-Z0-9.-]+\.[A-Z]{2,4}$/i.test(values.email)
    ) {
      errors.email = 'Invalid email address';
    }
    return errors;
  }

  /**
   * Formik submission handler.  
   * 
   * @param values {Values} the form values to submit
   * @param formikHelpers {FormikHelpers<Values>} in this case <code>setStatus</code> to set the form status after successful / failed submission
   * and <code>setSubmitting</code> to change submission state both on error or success
   * 
   * @see https://stackoverflow.com/questions/52986962/how-to-properly-use-formiks-seterror-method-react-library
   * @see https://stackoverflow.com/questions/58484239/redirect-doesnt-work-while-this-props-history-does 
   * @see https://stackoverflow.com/questions/56784155/how-to-link-to-next-page-url-in-handlesubmit-formik
   */
  const handleSubmit = async (values: Values, { setStatus, setSubmitting }: FormikHelpers<Values>) => {
    try  {
      
      const { data } = await signupUser({ variables: { userInput: values }});
     
      console.log("Data", data);
      console.log("Created user with ID: ", data.signUpWithEmail.userId);

      setSubmitting(false);

      //TODO: redirect to info page
      props.history.replace({
        pathname: '/users/info',
        state: {
          isError: true,
          buttonText:"Go to Login",
          buttonDestination:"/login",
          message:"Your account has been created. An email has been sent for verification purposes." 
        }
      });
      //props.history.replace("/login");

    } catch (err) {
      // Repeat setSubmitting call instead of finally block otherwise React complains
      // about changing state in an unmounted component
      setSubmitting(false)
      console.log(err);
      setStatus("Error creating user: " + err.message);
    }
  }

  return (  
        <Formik 
          initialValues={initialValues}
          validate={performValidation}
          onSubmit={handleSubmit}
          validateOnChange={false}
          component={InnerSignupForm} />
  );
}

const InnerSignupForm = ({ 
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
          
          <TextField
            variant="outlined"
            margin="normal"
            fullWidth
            id="email"
            label="Email Address"
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
            name="password"
            label="Password"
            type="password"
            id="password"
            onChange={handleChange}
            value={values.password}
          />
          
          <TextField
            variant="outlined"
            margin="normal"
            fullWidth
            name="repeatedPassword"
            label="Repeat password"
            type="password"
            id="repeatedPassword"
            onChange={handleChange}
            value={values.repeatedPassword}
          />

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
            { isSubmitting ? "Submitting" : "Submit" }  
          </Button>
          { !!status && <div> {status} </div>}
    </Form>
)

export default withRouter(SignupForm);
