import React, { Component } from "react";
import { connect, useDispatch } from "react-redux";
import { logoutUser } from "../actions";
import { RootState } from "../reducers"
import { Dispatch } from 'redux';

type ProtectedComponentProps = {
    isLoggingOut: boolean
    logoutError: boolean
    //dispatch: Dispatch
}

const Protected: React.FC<ProtectedComponentProps> = (props: ProtectedComponentProps) => {
    const dispatch = useDispatch();
    
    const handleLogout = () => {
       
        //const { dispatch } = props;
        const logoutDispatcher = logoutUser();
        logoutDispatcher(dispatch);
        //dispatch(logoutUser());
    };
    const { isLoggingOut, logoutError } = props;

    return (
        <div>
            <h1>This is your app's protected area.</h1>
            <p>Any routes here will also be protected</p>
            <button onClick={handleLogout}>Logout</button>
            {isLoggingOut && <p>Logging Out....</p>}
            {logoutError && <p>Error logging out</p>}
        </div>
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