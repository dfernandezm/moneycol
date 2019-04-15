const API = 'http://localhost:4000/search?qs=';

const searchApiCall = (searchTerm) => {
    return fetch(API + searchTerm)
              .then(response => response.json())
              .then(data => {
                console.log("Data is: ", data.searchResults);
                console.log("Results is: ", data.searchResults.results);
                return data.searchResults.results;
            });
};

export default {searchApiCall};