import React from 'react';
import TextField from "@material-ui/core/TextField";
import Button from "@material-ui/core/Button";
import { Values } from "./collectionsForm"
import { Form, FormikProps } from 'formik';

/**
 * Static Form receiving prepared Formik values
 * 
 */
const CollectionsFormStatic: React.FC<FormikProps<Values>> = ({
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
                id="name"
                label="Name"
                name="name"
                type="text"
                onChange={handleChange}
                value={values.name}
            />

            {   //TODO: component for errors
                errors.name ? <div>{errors.name}</div> : null
            }

            <TextField
                variant="outlined"
                margin="normal"
                fullWidth
                name="description"
                label="Description"
                id="description"
                type="text"
                onChange={handleChange}
                value={values.description}
            />

            {   //TODO: component for errors
                // or style for errors: https://material-ui.com/components/text-fields/ 
                errors.name ? <div>{errors.name}</div> : null
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

            {!!status && <div>{status}</div>}
        </Form>
    )

export { CollectionsFormStatic };