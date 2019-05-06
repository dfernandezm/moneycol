import React from 'react';
import { Redirect } from "react-router-dom";

export default function RenderRedirect({ termUsed, searchResults }) {
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