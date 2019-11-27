import React from 'react';
import { Redirect } from "react-router-dom";
import { useQuery } from '@apollo/react-hooks';

import { SearchResult } from './types/SearchResult';
import { SEARCH_GQL } from './gql/search';

type RedirectToSearchPageProps = {
    termUsed?: string,
    searchResults?: SearchResult[]
}

const RenderRedirect: React.FC<RedirectToSearchPageProps> = ({ termUsed, searchResults }) => {
    console.log("Term usequery: ", termUsed);
    const { data, loading, error } = useQuery(SEARCH_GQL, {
        variables: { searchTerm: termUsed },
    });

    if (loading) return <p>Loading...</p>;
    if (error) return <p>Error</p>;
    if (!loading) {
        console.log("Data: ", data.search.results);
    }

    return (
        <Redirect to={
            {
                pathname: '/searchResultsPage',
                search: '?qs=' + termUsed,
                state: { searchTerm: termUsed, searchResults: data.search ? data.search.results : [] }
            }
        } />
    )
}

export default RenderRedirect;