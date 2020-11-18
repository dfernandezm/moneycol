import React from 'react';
import { useMutation } from "@apollo/react-hooks";
import { Formik, FormikHelpers } from 'formik';
import { CREATE_COLLECTION_MUTATION } from "./gql/createCollection";
import { RouteComponentProps, withRouter } from 'react-router-dom';
import { CollectionsFormStatic } from './collectionsStaticForm';

interface CollectionData {
    name: string,
    description: string,
}

export type Values = CollectionData;

/**
 * Prepare Formik behaviour
 * 
 * @param props 
 */

const CollectionsForm: React.FC<RouteComponentProps> = (props: RouteComponentProps) => {

    const [createCollection] = useMutation(CREATE_COLLECTION_MUTATION);

    /**
     * Initialise form values 
     * 
     */
    const initialValues: Values = {
        name: '',
        description: '',
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
        
        if (!values.name) {
            errors.name = 'Required';
        }

        if (!values.description) {
            errors.description = 'Required';
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

            const createCollectionInput = values;
            const { data } = await createCollection({ variables: { col: createCollectionInput } });

            console.log("Created Collection", data.createCollection);

            setSubmitting(false);

            props.history.replace("/collections");

        } catch (err) {
            // setSubmitting call is repeated here instead of in a finally block otherwise React complains
            // about changing state in an unmounted component
            setSubmitting(false)
            console.log(err);

            // TODO: this error gives too much information, create a function to give meaningful error messages
            setStatus("Error creating collection: " + err.message);
        }
    }

    return (
        <Formik
            initialValues={initialValues}
            validate={performValidation}
            onSubmit={handleSubmit}
            validateOnChange={false}
            component={CollectionsFormStatic} />
    );
}

export default withRouter(CollectionsForm);