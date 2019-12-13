import React, { Component } from "react";
import { connect } from "react-redux";
import { logoutUser } from "../actions";

const Protected: React.FC = (props: any) => {
    const handleLogout = () => {
        const { dispatch } = props;
        dispatch(logoutUser());
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

function mapStateToProps(state) {
    return {
        isLoggingOut: state.auth.isLoggingOut,
        logoutError: state.auth.logoutError
    };
}

export default connect(mapStateToProps)(Protected);