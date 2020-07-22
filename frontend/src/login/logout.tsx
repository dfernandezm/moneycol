import React from "react";
import { connect, useDispatch } from "react-redux";
import { logoutUser } from "./actions";
import { RootState } from "./reducers"
import { useApolloClient } from '@apollo/react-hooks';

import Button from "@material-ui/core/Button";

type LogoutComponentProps = {
    isLoggingOut: boolean
    logoutError: boolean
}

const Logout: React.FC<LogoutComponentProps> = (props: LogoutComponentProps) => {

    const dispatch = useDispatch();
    const apolloClient = useApolloClient();

    const handleLogout = () => {
        // doing it this way we don't pick 'dispatch' from the props but from the hook
        const logoutDispatcher = logoutUser();
        logoutDispatcher(dispatch, {}, apolloClient);
    };

    const { isLoggingOut, logoutError } = props;

    return (
        <>
        <Button
            type="button"
            fullWidth
            variant="contained"
            color="primary"
            onClick={handleLogout}>
            {isLoggingOut ? "Logging out" : "Logout"}
        </Button>
        { logoutError && <div>Error logging out</div>}
        </>

    );
}

// Map Redux state to props for this component
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

export default connector(Logout);