import React from "react";
import { connect, useDispatch } from "react-redux";
import { logoutUser } from "./actions";
import { RootState } from "./reducers"

import Container from '@material-ui/core/Container';
import Typography from "@material-ui/core/Typography";
import Button from "@material-ui/core/Button";
import { makeStyles, Theme, createStyles } from '@material-ui/core/styles';

type ProtectedComponentProps = {
    isLoggingOut: boolean
    logoutError: boolean
}

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

const Protected: React.FC<ProtectedComponentProps> = (props: ProtectedComponentProps) => {
    const classes = useStyles();
    const dispatch = useDispatch();
    
    const handleLogout = () => {
        // doing it this way we don't pick 'dispatch' from the props but from the hook
        const logoutDispatcher = logoutUser();
        logoutDispatcher(dispatch);
    };

    const { isLoggingOut, logoutError } = props;

    const loggingOutComponent =  
    <Typography gutterBottom variant="body2" component="p">
       Logging out...
    </Typography>;
     const errorLoginOutComponent =  
     <Typography gutterBottom variant="body2" component="p">
        Error logging out
     </Typography>

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

const mapState = (state: RootState) => {
    return {
        isLoggingOut: state.auth.isLoggingOut,
        logoutError: state.auth.logoutError
    };
}

// empty
const mapDispatch = {}

const connector = connect(
    mapState,
    mapDispatch
)

export default connector(Protected);