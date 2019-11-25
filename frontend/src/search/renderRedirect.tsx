import React from 'react';
import { Redirect } from "react-router-dom";

type RedirectToSearchPageProps = {
    termUsed?: string,
    searchResults?: []
}

const RenderRedirect: React.FC<RedirectToSearchPageProps> = ({ termUsed, searchResults }) => {
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