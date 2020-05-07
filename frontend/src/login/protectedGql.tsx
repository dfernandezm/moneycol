import React,{ useState } from "react";
import Container from '@material-ui/core/Container';
import Typography from "@material-ui/core/Typography";
import Button from "@material-ui/core/Button";
import { makeStyles, Theme, createStyles } from '@material-ui/core/styles';

import { Redirect } from "react-router-dom";
import { useMutation } from '@apollo/react-hooks';
import { LOGOUT_GQL } from './gql/logout';

// type ProtectedComponentProps = {
//     isLoggingOut: boolean
//     logoutError: boolean
// }

const useStyles = makeStyles((theme: Theme) =>
    createStyles({
       logoutButton: {
           width: 150,
           textAlign: 'left'
       },
       label: {
        textTransform: 'capitalize',
      }
    }));

const Protected: React.FC = () => {
    const classes = useStyles();
    const [ logoutUser, { loading: isLoggingOut, error: logoutError }] = useMutation(LOGOUT_GQL);
    const [ loggedOut, setLoggedOut ] = useState(false);

    const handleLogout = async () => {
        // doing it this way we don't pick 'dispatch' from the props but from the hook
        // const logoutDispatcher = logoutUser();
        // logoutDispatcher(dispatch);
        try {
            const { data: { logout } } = await logoutUser();
            console.log("Logout success:", logout.result);
            localStorage.removeItem("token");
            setLoggedOut(true);
        } catch (error) {
            console.log("logout error", error);
        }
    };

    //const { isLoggingOut, logoutError } = props;

    const loggingOutComponent =  
    <Typography gutterBottom variant="body2" component="p">
       Logging out...
    </Typography>;
     const errorLoginOutComponent =  
     <Typography gutterBottom variant="body2" component="p">
        Error logging out
     </Typography>

    if (loggedOut) {
        return <Redirect to="/login" />
    } else {
    return (
        <Container maxWidth="lg">
            <Typography gutterBottom variant="h3" component="h3">
                This is your app's protected area.
            </Typography>
            <Typography gutterBottom variant="body2" component="p">
                Any routes here will also be protected
            </Typography>

            <Button
                type="button"
                fullWidth
                variant="contained"
                color="primary"
                classes={{
                    label: classes.label
                }}
                className={classes.logoutButton}
                onClick={handleLogout}>
                Logout
            </Button>
        
            {isLoggingOut && loggingOutComponent }
            {logoutError && errorLoginOutComponent}
        </Container>
    );
            }
}

export default Protected;