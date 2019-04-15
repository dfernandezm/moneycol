const API = 'http://localhost:4000/search?qs=';

//TODO: sanitize search term before sending (lowercase etc)
const searchApiCall = (searchTerm) => {
    return fetch(API + searchTerm)
              .then(response => response.json())
              .then(data => data.searchResults.results);
};

export default {searchApiCall};