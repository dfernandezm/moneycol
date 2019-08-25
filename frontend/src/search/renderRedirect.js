import React from 'react';
import { Redirect } from "react-router-dom";

const RenderRedirect = ({ termUsed, searchResults }) => {
    return (
        <Redirect to={
            {
            pathname: '/searchResultsPage',
            search: '?qs=' + termUsed,
            state: { results: searchResults }
            }
        } />
    )
}

export default RenderRedirect;