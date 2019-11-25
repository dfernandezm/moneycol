import React from 'react';
import { Redirect } from "react-router-dom";
import { SearchResult } from './types/SearchResult';

type RedirectToSearchPageProps = {
    termUsed?: string,
    searchResults?: SearchResult[]
}

const RenderRedirect: React.FC<RedirectToSearchPageProps> = ({ termUsed, searchResults }) => {
    return (
        <Redirect to={
            {
                pathname: '/searchResultsPage',
                search: '?qs=' + termUsed,
                state: { searchResults: searchResults }
            }
        } />
    )
}

export default RenderRedirect;