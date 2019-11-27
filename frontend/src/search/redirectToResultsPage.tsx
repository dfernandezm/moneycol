import React from 'react';
import { Redirect } from "react-router-dom";
import { useQuery } from '@apollo/react-hooks';
import { SEARCH_GQL } from './gql/search';

type RedirectToSearchPageProps = {
    termUsed: string
}

const RenderRedirect: React.FC<RedirectToSearchPageProps> = ({ termUsed }) => {
    //TODO: this useQuery shouldn't be here possibly, better in the SearchResultsPage?
    const { data, loading, error } = useQuery(SEARCH_GQL, {
        variables: { searchTerm: termUsed },
    });

    if (loading) return <p>&nbsp;</p>;
    if (error) return <p>Error: {error}</p>;

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